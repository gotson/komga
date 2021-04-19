package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

@Service
class BookLifecycle(
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val readListRepository: ReadListRepository,
  private val bookAnalyzer: BookAnalyzer,
  private val imageConverter: ImageConverter
) {

  fun analyzeAndPersist(book: Book): Boolean {
    logger.info { "Analyze and persist book: $book" }
    val media = try {
      bookAnalyzer.analyze(book)
    } catch (ade: AccessDeniedException) {
      logger.error(ade) { "Error while analyzing book: $book" }
      Media(status = Media.Status.ERROR, comment = "ERR_1000")
    } catch (ex: Exception) {
      logger.error(ex) { "Error while analyzing book: $book" }
      Media(status = Media.Status.ERROR, comment = "ERR_1005")
    }.copy(bookId = book.id)

    // if the number of pages has changed, delete all read progress for that book
    mediaRepository.findById(book.id).let { previous ->
      if (previous.status == Media.Status.OUTDATED && previous.pages.size != media.pages.size) {
        readProgressRepository.deleteByBookId(book.id)
      }
    }

    mediaRepository.update(media)
    return media.status == Media.Status.READY
  }

  fun generateThumbnailAndPersist(book: Book) {
    logger.info { "Generate thumbnail and persist for book: $book" }
    try {
      addThumbnailForBook(bookAnalyzer.generateThumbnail(BookWithMedia(book, mediaRepository.findById(book.id))))
    } catch (ex: Exception) {
      logger.error(ex) { "Error while creating thumbnail" }
    }
  }

  fun addThumbnailForBook(thumbnail: ThumbnailBook) {
    when (thumbnail.type) {
      ThumbnailBook.Type.GENERATED -> {
        // only one generated thumbnail is allowed
        thumbnailBookRepository.deleteByBookIdAndType(thumbnail.bookId, ThumbnailBook.Type.GENERATED)
        thumbnailBookRepository.insert(thumbnail)
      }
      ThumbnailBook.Type.SIDECAR -> {
        // delete existing thumbnail with the same url
        thumbnailBookRepository.findByBookIdAndType(thumbnail.bookId, ThumbnailBook.Type.SIDECAR)
          .filter { it.url == thumbnail.url }
          .forEach {
            thumbnailBookRepository.delete(it.id)
          }
        thumbnailBookRepository.insert(thumbnail)
      }
    }

    if (thumbnail.selected)
      thumbnailBookRepository.markSelected(thumbnail)
    else
      thumbnailsHouseKeeping(thumbnail.bookId)
  }

  fun getThumbnail(bookId: String): ThumbnailBook? {
    val selected = thumbnailBookRepository.findSelectedByBookId(bookId)

    if (selected == null || !selected.exists()) {
      thumbnailsHouseKeeping(bookId)
      return thumbnailBookRepository.findSelectedByBookId(bookId)
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

  private fun thumbnailsHouseKeeping(bookId: String) {
    logger.info { "House keeping thumbnails for book: $bookId" }
    val all = thumbnailBookRepository.findByBookId(bookId)
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

  private fun ThumbnailBook.exists(): Boolean {
    if (type == ThumbnailBook.Type.SIDECAR) {
      if (url != null)
        return Files.exists(Paths.get(url.toURI()))
      return false
    }
    return true
  }

  @Throws(
    ImageConversionException::class,
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class
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

  fun deleteOne(bookId: String) {
    logger.info { "Delete book id: $bookId" }

    readProgressRepository.deleteByBookId(bookId)
    readListRepository.removeBookFromAll(bookId)

    mediaRepository.delete(bookId)
    thumbnailBookRepository.deleteByBookId(bookId)
    bookMetadataRepository.delete(bookId)

    bookRepository.delete(bookId)
  }

  fun deleteMany(bookIds: Collection<String>) {
    logger.info { "Delete all books: $bookIds" }

    readProgressRepository.deleteByBookIds(bookIds)
    readListRepository.removeBookFromAll(bookIds)

    mediaRepository.deleteByBookIds(bookIds)
    thumbnailBookRepository.deleteByBookIds(bookIds)
    bookMetadataRepository.deleteByBookIds(bookIds)

    bookRepository.deleteByBookIds(bookIds)
  }

  fun markReadProgress(book: Book, user: KomgaUser, page: Int) {
    val media = mediaRepository.findById(book.id)
    require(page >= 1 && page <= media.pages.size) { "Page argument ($page) must be within 1 and book page count (${media.pages.size})" }

    readProgressRepository.save(ReadProgress(book.id, user.id, page, page == media.pages.size))
  }

  fun markReadProgressCompleted(bookId: String, user: KomgaUser) {
    val media = mediaRepository.findById(bookId)

    readProgressRepository.save(ReadProgress(bookId, user.id, media.pages.size, true))
  }

  fun deleteReadProgress(bookId: String, user: KomgaUser) {
    readProgressRepository.delete(bookId, user.id)
  }
}
