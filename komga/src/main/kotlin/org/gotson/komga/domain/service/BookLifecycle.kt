package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.events.EventPublisher
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.HistoricalEvent
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.infrastructure.hash.Hasher
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.io.File
import java.time.LocalDateTime
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isWritable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists
import kotlin.io.path.toPath

private val logger = KotlinLogging.logger {}

@Service
class BookLifecycle(
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val readListRepository: ReadListRepository,
  private val libraryRepository: LibraryRepository,
  private val bookAnalyzer: BookAnalyzer,
  private val imageConverter: ImageConverter,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
  private val hasher: Hasher,
  private val historicalEventRepository: HistoricalEventRepository,
) {

  fun analyzeAndPersist(book: Book): Boolean {
    logger.info { "Analyze and persist book: $book" }
    val media = bookAnalyzer.analyze(book, libraryRepository.findById(book.libraryId).analyzeDimensions)

    transactionTemplate.executeWithoutResult {
      // if the number of pages has changed, delete all read progress for that book
      mediaRepository.findById(book.id).let { previous ->
        if (previous.status == Media.Status.OUTDATED && previous.pages.size != media.pages.size) {
          logger.info { "Number of pages differ, reset read progress for book" }
          readProgressRepository.deleteByBookId(book.id)
        }
      }

      mediaRepository.update(media)
    }

    eventPublisher.publishEvent(DomainEvent.BookUpdated(book))

    return media.status == Media.Status.READY
  }

  fun hashAndPersist(book: Book) {
    if (!libraryRepository.findById(book.libraryId).hashFiles)
      return logger.info { "File hashing is disabled for the library, it may have changed since the task was submitted, skipping" }

    logger.info { "Hash and persist book: $book" }
    if (book.fileHash.isBlank()) {
      val hash = hasher.computeHash(book.path)
      bookRepository.update(book.copy(fileHash = hash))
    } else {
      logger.info { "Book already has a hash, skipping" }
    }
  }

  fun hashPagesAndPersist(book: Book) {
    if (!libraryRepository.findById(book.libraryId).hashPages)
      return logger.info { "Page hashing is disabled for the library, it may have changed since the task was submitted, skipping" }

    logger.info { "Hash and persist pages for book: $book" }

    mediaRepository.update(bookAnalyzer.hashPages(BookWithMedia(book, mediaRepository.findById(book.id))))
  }

  fun generateThumbnailAndPersist(book: Book) {
    logger.info { "Generate thumbnail and persist for book: $book" }
    try {
      addThumbnailForBook(bookAnalyzer.generateThumbnail(BookWithMedia(book, mediaRepository.findById(book.id))), MarkSelectedPreference.IF_NONE_OR_GENERATED)
    } catch (ex: Exception) {
      logger.error(ex) { "Error while creating thumbnail" }
    }
  }

  fun addThumbnailForBook(thumbnail: ThumbnailBook, markSelected: MarkSelectedPreference): ThumbnailBook {
    when (thumbnail.type) {
      ThumbnailBook.Type.GENERATED -> {
        // only one generated thumbnail is allowed
        thumbnailBookRepository.deleteByBookIdAndType(thumbnail.bookId, ThumbnailBook.Type.GENERATED)
        thumbnailBookRepository.insert(thumbnail.copy(selected = false))
      }
      ThumbnailBook.Type.SIDECAR -> {
        // delete existing thumbnail with the same url
        thumbnailBookRepository.findAllByBookIdAndType(thumbnail.bookId, ThumbnailBook.Type.SIDECAR)
          .filter { it.url == thumbnail.url }
          .forEach {
            thumbnailBookRepository.delete(it.id)
          }
        thumbnailBookRepository.insert(thumbnail.copy(selected = false))
      }
      ThumbnailBook.Type.USER_UPLOADED -> {
        thumbnailBookRepository.insert(thumbnail.copy(selected = false))
      }
    }

    val selected = when (markSelected) {
      MarkSelectedPreference.YES -> true
      MarkSelectedPreference.IF_NONE_OR_GENERATED -> {
        val selectedThumbnail = thumbnailBookRepository.findSelectedByBookIdOrNull(thumbnail.bookId)
        selectedThumbnail == null || selectedThumbnail.type == ThumbnailBook.Type.GENERATED
      }
      MarkSelectedPreference.NO -> false
    }

    if (selected) thumbnailBookRepository.markSelected(thumbnail)
    else thumbnailsHouseKeeping(thumbnail.bookId)

    val newThumbnail = thumbnail.copy(selected = selected)
    eventPublisher.publishEvent(DomainEvent.ThumbnailBookAdded(newThumbnail))
    return newThumbnail
  }

  fun deleteThumbnailForBook(thumbnail: ThumbnailBook) {
    require(thumbnail.type == ThumbnailBook.Type.USER_UPLOADED) { "Only uploaded thumbnails can be deleted" }
    thumbnailBookRepository.delete(thumbnail.id)
    thumbnailsHouseKeeping(thumbnail.bookId)
    eventPublisher.publishEvent(DomainEvent.ThumbnailBookDeleted(thumbnail))
  }

  fun getThumbnail(bookId: String): ThumbnailBook? {
    val selected = thumbnailBookRepository.findSelectedByBookIdOrNull(bookId)

    if (selected == null || !selected.exists()) {
      thumbnailsHouseKeeping(bookId)
      return thumbnailBookRepository.findSelectedByBookIdOrNull(bookId)
    }

    return selected
  }

  fun getThumbnailBytes(bookId: String): ByteArray? {
    getThumbnail(bookId)?.let {
      return when {
        it.thumbnail != null -> it.thumbnail
        it.url != null -> File(it.url.toURI()).readBytes()
        else -> null
      }
    }
    return null
  }

  fun getThumbnailBytesByThumbnailId(thumbnailId: String): ByteArray? =
    thumbnailBookRepository.findByIdOrNull(thumbnailId)?.let {
      getBytesFromThumbnailBook(it)
    }

  private fun getBytesFromThumbnailBook(thumbnail: ThumbnailBook): ByteArray? =
    when {
      thumbnail.thumbnail != null -> thumbnail.thumbnail
      thumbnail.url != null -> File(thumbnail.url.toURI()).readBytes()
      else -> null
    }

  private fun thumbnailsHouseKeeping(bookId: String) {
    logger.info { "House keeping thumbnails for book: $bookId" }
    val all = thumbnailBookRepository.findAllByBookId(bookId)
      .mapNotNull {
        if (!it.exists()) {
          logger.warn { "Thumbnail doesn't exist, removing entry" }
          thumbnailBookRepository.delete(it.id)
          null
        } else it
      }

    val selected = all.filter { it.selected }
    when {
      selected.size > 1 -> {
        logger.info { "More than one thumbnail is selected, removing extra ones" }
        thumbnailBookRepository.markSelected(selected[0])
      }
      selected.isEmpty() && all.isNotEmpty() -> {
        logger.info { "Book has no selected thumbnail, choosing one automatically" }
        thumbnailBookRepository.markSelected(all.first())
      }
    }
  }

  @Throws(
    ImageConversionException::class,
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class,
  )
  fun getBookPage(book: Book, number: Int, convertTo: ImageType? = null, resizeTo: Int? = null): BookPageContent {
    val media = mediaRepository.findById(book.id)
    val pageContent = bookAnalyzer.getPageContent(BookWithMedia(book, mediaRepository.findById(book.id)), number)
    val pageMediaType = media.pages[number - 1].mediaType

    if (resizeTo != null) {
      val targetFormat = ImageType.JPEG
      val convertedPage = try {
        imageConverter.resizeImage(pageContent, targetFormat.imageIOFormat, resizeTo)
      } catch (e: Exception) {
        logger.error(e) { "Resize page #$number of book $book to $resizeTo: failed" }
        throw e
      }
      return BookPageContent(number, convertedPage, targetFormat.mediaType)
    } else {
      convertTo?.let {
        val msg = "Convert page #$number of book $book from $pageMediaType to ${it.mediaType}"
        if (!imageConverter.supportedReadMediaTypes.contains(pageMediaType)) {
          throw ImageConversionException("$msg: unsupported read format $pageMediaType")
        }
        if (!imageConverter.supportedWriteMediaTypes.contains(it.mediaType)) {
          throw ImageConversionException("$msg: unsupported write format ${it.mediaType}")
        }
        if (pageMediaType == it.mediaType) {
          logger.warn { "$msg: same format, no need for conversion" }
          return@let
        }

        logger.info { msg }
        val convertedPage = try {
          imageConverter.convertImage(pageContent, it.imageIOFormat)
        } catch (e: Exception) {
          logger.error(e) { "$msg: conversion failed" }
          throw e
        }
        return BookPageContent(number, convertedPage, it.mediaType)
      }

      return BookPageContent(number, pageContent, pageMediaType)
    }
  }

  fun deleteOne(book: Book) {
    logger.info { "Delete book id: ${book.id}" }

    transactionTemplate.executeWithoutResult {
      readProgressRepository.deleteByBookId(book.id)
      readListRepository.removeBookFromAll(book.id)

      mediaRepository.delete(book.id)
      thumbnailBookRepository.deleteByBookId(book.id)
      bookMetadataRepository.delete(book.id)

      bookRepository.delete(book.id)
    }

    eventPublisher.publishEvent(DomainEvent.BookDeleted(book))
  }

  fun softDeleteMany(books: Collection<Book>) {
    logger.info { "Soft delete books: $books" }
    val deletedDate = LocalDateTime.now()
    bookRepository.update(books.map { it.copy(deletedDate = deletedDate) })

    books.forEach { eventPublisher.publishEvent(DomainEvent.BookUpdated(it)) }
  }

  fun deleteMany(books: Collection<Book>) {
    val bookIds = books.map { it.id }
    logger.info { "Delete book ids: $bookIds" }

    transactionTemplate.executeWithoutResult {
      readProgressRepository.deleteByBookIds(bookIds)
      readListRepository.removeBooksFromAll(bookIds)

      mediaRepository.deleteByBookIds(bookIds)
      thumbnailBookRepository.deleteByBookIds(bookIds)
      bookMetadataRepository.delete(bookIds)

      bookRepository.delete(bookIds)
    }

    books.forEach { eventPublisher.publishEvent(DomainEvent.BookDeleted(it)) }
  }

  fun markReadProgress(book: Book, user: KomgaUser, page: Int) {
    val pages = mediaRepository.getPagesSize(book.id)
    require(page in 1..pages) { "Page argument ($page) must be within 1 and book page count ($pages)" }

    val progress = ReadProgress(book.id, user.id, page, page == pages)
    readProgressRepository.save(progress)
    eventPublisher.publishEvent(DomainEvent.ReadProgressChanged(progress))
  }

  fun markReadProgressCompleted(bookId: String, user: KomgaUser) {
    val media = mediaRepository.findById(bookId)

    val progress = ReadProgress(bookId, user.id, media.pages.size, true)
    readProgressRepository.save(progress)
    eventPublisher.publishEvent(DomainEvent.ReadProgressChanged(progress))
  }

  fun deleteReadProgress(book: Book, user: KomgaUser) {
    readProgressRepository.findByBookIdAndUserIdOrNull(book.id, user.id)?.let { progress ->
      readProgressRepository.delete(book.id, user.id)
      eventPublisher.publishEvent(DomainEvent.ReadProgressDeleted(progress))
    }
  }

  fun deleteBookFiles(book: Book) {
    if (book.path.notExists()) return logger.info { "Cannot delete book file, path does not exist: ${book.path}" }
    if (!book.path.isWritable()) return logger.info { "Cannot delete book file, path is not writable: ${book.path}" }

    val thumbnails = thumbnailBookRepository.findAllByBookIdAndType(book.id, ThumbnailBook.Type.SIDECAR)
      .mapNotNull { it.url?.toURI()?.toPath() }
      .filter { it.exists() && it.isWritable() }

    if (book.path.deleteIfExists()) {
      logger.info { "Deleted file: ${book.path}" }
      historicalEventRepository.insert(HistoricalEvent.BookFileDeleted(book, "File was deleted by user request"))
    }
    thumbnails.forEach {
      if (it.deleteIfExists()) logger.info { "Deleted file: $it" }
    }

    if (book.path.parent.listDirectoryEntries().isEmpty())
      if (book.path.parent.deleteIfExists()) {
        logger.info { "Deleted directory: ${book.path.parent}" }
        historicalEventRepository.insert(HistoricalEvent.SeriesFolderDeleted(book.seriesId, book.path.parent, "Folder was deleted because it was empty"))
      }

    softDeleteMany(listOf(book))
  }
}
