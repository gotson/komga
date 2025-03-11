package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookAction
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.HistoricalEvent
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.NoThumbnailFoundException
import org.gotson.komga.domain.model.R2Progression
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.TypedBytes
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.infrastructure.hash.Hasher
import org.gotson.komga.infrastructure.hash.KoreaderHasher
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.io.File
import java.net.URLDecoder
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isWritable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists
import kotlin.io.path.toPath
import kotlin.math.roundToInt

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
  private val eventPublisher: ApplicationEventPublisher,
  private val transactionTemplate: TransactionTemplate,
  private val hasher: Hasher,
  private val hasherKoreader: KoreaderHasher,
  private val historicalEventRepository: HistoricalEventRepository,
  private val komgaSettingsProvider: KomgaSettingsProvider,
  @Qualifier("pdfImageType")
  private val pdfImageType: ImageType,
) {
  private val resizeTargetFormat = ImageType.JPEG

  fun analyzeAndPersist(book: Book): Set<BookAction> {
    logger.info { "Analyze and persist book: $book" }
    val media = bookAnalyzer.analyze(book, libraryRepository.findById(book.libraryId).analyzeDimensions)

    transactionTemplate.executeWithoutResult {
      // if the number of pages has changed, delete all read progress for that book
      mediaRepository.findById(book.id).let { previous ->
        if (previous.status == Media.Status.OUTDATED && previous.pageCount != media.pageCount) {
          val adjustedProgress =
            readProgressRepository
              .findAllByBookId(book.id)
              .map { it.copy(page = if (it.completed) media.pageCount else 1) }
          if (adjustedProgress.isNotEmpty()) {
            logger.info { "Number of pages differ, adjust read progress for book" }
            readProgressRepository.save(adjustedProgress)
          }
        }
      }

      mediaRepository.update(media)
    }

    eventPublisher.publishEvent(DomainEvent.BookUpdated(book))

    return if (media.status == Media.Status.READY) setOf(BookAction.GENERATE_THUMBNAIL, BookAction.REFRESH_METADATA) else emptySet()
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

  fun hashKoreaderAndPersist(book: Book) {
    if (!libraryRepository.findById(book.libraryId).hashKoreader)
      return logger.info { "File hashing for Koreader is disabled for the library, it may have changed since the task was submitted, skipping" }

    logger.info { "Hash Koreader and persist book: $book" }
    if (book.fileHashKoreader.isBlank()) {
      val hash = hasherKoreader.computeHash(book.path)
      bookRepository.update(book.copy(fileHashKoreader = hash))
    } else {
      logger.info { "Book already has a Koreader hash, skipping" }
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
    } catch (ex: NoThumbnailFoundException) {
      logger.error { "Error while creating thumbnail" }
    } catch (ex: Exception) {
      logger.error(ex) { "Error while creating thumbnail" }
    }
  }

  fun addThumbnailForBook(
    thumbnail: ThumbnailBook,
    markSelected: MarkSelectedPreference,
  ): ThumbnailBook {
    when (thumbnail.type) {
      ThumbnailBook.Type.GENERATED -> {
        // only one generated thumbnail is allowed
        thumbnailBookRepository.deleteByBookIdAndType(thumbnail.bookId, ThumbnailBook.Type.GENERATED)
        thumbnailBookRepository.insert(thumbnail.copy(selected = false))
      }

      ThumbnailBook.Type.SIDECAR -> {
        // delete existing thumbnail with the same url
        thumbnailBookRepository
          .findAllByBookIdAndType(thumbnail.bookId, setOf(ThumbnailBook.Type.SIDECAR))
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

    val selected =
      when (markSelected) {
        MarkSelectedPreference.YES -> true
        MarkSelectedPreference.IF_NONE_OR_GENERATED -> {
          val selectedThumbnail = thumbnailBookRepository.findSelectedByBookIdOrNull(thumbnail.bookId)
          selectedThumbnail == null || selectedThumbnail.type == ThumbnailBook.Type.GENERATED
        }

        MarkSelectedPreference.NO -> false
      }

    if (selected)
      thumbnailBookRepository.markSelected(thumbnail)
    else
      thumbnailsHouseKeeping(thumbnail.bookId)

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

  fun getThumbnailBytes(
    bookId: String,
    resizeTo: Int? = null,
  ): TypedBytes? {
    getThumbnail(bookId)?.let {
      val thumbnailBytes =
        when {
          it.thumbnail != null -> it.thumbnail
          it.url != null -> File(it.url.toURI()).readBytes()
          else -> return null
        }

      if (resizeTo != null) {
        try {
          return TypedBytes(
            imageConverter.resizeImageToByteArray(thumbnailBytes, resizeTargetFormat, resizeTo),
            resizeTargetFormat.mediaType,
          )
        } catch (e: Exception) {
          logger.error(e) { "Resize thumbnail of book $bookId to $resizeTo: failed" }
        }
      }

      return TypedBytes(thumbnailBytes, it.mediaType)
    }
    return null
  }

  fun getThumbnailBytesOriginal(bookId: String): TypedBytes? {
    val thumbnail = getThumbnail(bookId) ?: return null
    return if (thumbnail.type == ThumbnailBook.Type.GENERATED) {
      val book = bookRepository.findByIdOrNull(bookId) ?: return null
      val media = mediaRepository.findById(book.id)
      bookAnalyzer.getPoster(BookWithMedia(book, media))
    } else {
      getThumbnailBytes(bookId)
    }
  }

  fun getThumbnailBytesByThumbnailId(thumbnailId: String): TypedBytes? =
    thumbnailBookRepository.findByIdOrNull(thumbnailId)?.let { thumbnail ->
      getBytesFromThumbnailBook(thumbnail)?.let { bytes ->
        TypedBytes(bytes, thumbnail.mediaType)
      }
    }

  private fun getBytesFromThumbnailBook(thumbnail: ThumbnailBook): ByteArray? =
    when {
      thumbnail.thumbnail != null -> thumbnail.thumbnail
      thumbnail.url != null -> File(thumbnail.url.toURI()).readBytes()
      else -> null
    }

  private fun thumbnailsHouseKeeping(bookId: String) {
    logger.info { "House keeping thumbnails for book: $bookId" }
    val all =
      thumbnailBookRepository
        .findAllByBookId(bookId)
        .mapNotNull {
          if (!it.exists()) {
            logger.warn { "Thumbnail doesn't exist, removing entry" }
            thumbnailBookRepository.delete(it.id)
            null
          } else {
            it
          }
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

  fun findBookThumbnailsToRegenerate(forBiggerResultOnly: Boolean): Collection<String> =
    if (forBiggerResultOnly) {
      thumbnailBookRepository.findAllBookIdsByThumbnailTypeAndDimensionSmallerThan(ThumbnailBook.Type.GENERATED, komgaSettingsProvider.thumbnailSize.maxEdge)
    } else {
      bookRepository.findAll(SearchCondition.Deleted(SearchOperator.IsFalse), SearchContext.empty(), Pageable.unpaged()).content.map { it.id }
    }

  @Throws(
    ImageConversionException::class,
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class,
  )
  fun getBookPage(
    book: Book,
    number: Int,
    convertTo: ImageType? = null,
    resizeTo: Int? = null,
  ): TypedBytes {
    val media = mediaRepository.findById(book.id)
    val pageContent = bookAnalyzer.getPageContent(BookWithMedia(book, media), number)
    val pageMediaType =
      if (media.profile == MediaProfile.PDF)
        pdfImageType.mediaType
      else
        media.pages[number - 1].mediaType

    if (resizeTo != null) {
      val convertedPage =
        try {
          imageConverter.resizeImageToByteArray(pageContent, resizeTargetFormat, resizeTo)
        } catch (e: Exception) {
          logger.error(e) { "Resize page #$number of book $book to $resizeTo: failed" }
          throw e
        }
      return TypedBytes(convertedPage, resizeTargetFormat.mediaType)
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
        val convertedPage =
          try {
            imageConverter.convertImage(pageContent, it.imageIOFormat)
          } catch (e: Exception) {
            logger.error(e) { "$msg: conversion failed" }
            throw e
          }
        return TypedBytes(convertedPage, it.mediaType)
      }

      return TypedBytes(pageContent, pageMediaType)
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

      mediaRepository.delete(bookIds)
      thumbnailBookRepository.deleteByBookIds(bookIds)
      bookMetadataRepository.delete(bookIds)

      bookRepository.delete(bookIds)
    }

    books.forEach { eventPublisher.publishEvent(DomainEvent.BookDeleted(it)) }
  }

  fun markReadProgress(
    book: Book,
    user: KomgaUser,
    page: Int,
  ) {
    val media = mediaRepository.findById(book.id)
    require(page in 1..media.pageCount) { "Page argument ($page) must be within 1 and book page count (${media.pageCount})" }

    val locator =
      if (media.profile == MediaProfile.EPUB) {
        require(media.epubDivinaCompatible) { "epub book is not Divina compatible" }

        val extension =
          mediaRepository.findExtensionByIdOrNull(book.id) as? MediaExtensionEpub
            ?: throw IllegalArgumentException("Epub extension not found")
        extension.positions[page - 1]
      } else {
        null
      }

    val progress = ReadProgress(book.id, user.id, page, page == media.pageCount, locator = locator)

    readProgressRepository.save(progress)
    eventPublisher.publishEvent(DomainEvent.ReadProgressChanged(progress))
  }

  fun markReadProgressCompleted(
    bookId: String,
    user: KomgaUser,
  ) {
    val media = mediaRepository.findById(bookId)

    val progress = ReadProgress(bookId, user.id, media.pageCount, true)
    readProgressRepository.save(progress)
    eventPublisher.publishEvent(DomainEvent.ReadProgressChanged(progress))
  }

  fun deleteReadProgress(
    book: Book,
    user: KomgaUser,
  ) {
    readProgressRepository.findByBookIdAndUserIdOrNull(book.id, user.id)?.let { progress ->
      readProgressRepository.delete(book.id, user.id)
      eventPublisher.publishEvent(DomainEvent.ReadProgressDeleted(progress))
    }
  }

  fun markProgression(
    book: Book,
    user: KomgaUser,
    newProgression: R2Progression,
  ) {
    readProgressRepository.findByBookIdAndUserIdOrNull(book.id, user.id)?.let { savedProgress ->
      check(
        newProgression.modified
          .withZoneSameInstant(ZoneId.systemDefault())
          .toLocalDateTime()
          .isAfter(savedProgress.readDate),
      ) { "Progression is older than existing" }
    }

    val media = mediaRepository.findById(book.id)
    requireNotNull(media.profile) { "Media has no profile" }
    val progress =
      when (media.profile!!) {
        MediaProfile.DIVINA,
        MediaProfile.PDF,
        -> {
          require(newProgression.locator.locations?.position in 1..media.pageCount) { "Page argument (${newProgression.locator.locations?.position}) must be within 1 and book page count (${media.pageCount})" }
          ReadProgress(
            book.id,
            user.id,
            newProgression.locator.locations!!.position!!,
            newProgression.locator.locations.position == media.pageCount,
            newProgression.modified.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
            newProgression.device.id,
            newProgression.device.name,
            newProgression.locator,
          )
        }

        MediaProfile.EPUB -> {
          val href =
            newProgression.locator.href
              .replaceAfter("#", "")
              .removeSuffix("#")
              .let { URLDecoder.decode(it, Charsets.UTF_8) }
          require(href in media.files.map { it.fileName }) { "Resource does not exist in book: $href" }
          requireNotNull(newProgression.locator.locations?.progression) { "location.progression is required" }

          val extension =
            mediaRepository.findExtensionByIdOrNull(book.id) as? MediaExtensionEpub
              ?: throw IllegalArgumentException("Epub extension not found")
          // match progression with positions
          val matchingPositions = extension.positions.filter { it.href == href }
          val matchedPosition =
            if (extension.isFixedLayout && matchingPositions.size == 1)
              matchingPositions.first()
            else
              matchingPositions.firstOrNull { it.locations!!.progression == newProgression.locator.locations!!.progression }
                ?: run {
                  // no exact match
                  val before = matchingPositions.filter { it.locations!!.progression!! < newProgression.locator.locations!!.progression!! }.maxByOrNull { it.locations!!.position!! }
                  val after = matchingPositions.filter { it.locations!!.progression!! > newProgression.locator.locations!!.progression!! }.minByOrNull { it.locations!!.position!! }
                  if (before == null || after == null || before.locations!!.position!! > after.locations!!.position!!)
                    throw IllegalArgumentException("Invalid progression")
                  before
                }

          val totalProgression = matchedPosition.locations?.totalProgression
          ReadProgress(
            book.id,
            user.id,
            totalProgression?.let { (media.pageCount * it).roundToInt() } ?: 0,
            totalProgression?.let { it >= 0.99F } ?: false,
            newProgression.modified.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
            newProgression.device.id,
            newProgression.device.name,
            newProgression.locator.copy(
              // use the type we have instead of the one provided
              type = matchedPosition.type,
              // if no koboSpan is provided, use the one we matched
              koboSpan = newProgression.locator.koboSpan ?: matchedPosition.koboSpan,
              // don't trust the provided total progression, the one from Kobo can be wrong
              locations = newProgression.locator.locations?.copy(totalProgression = totalProgression),
            ),
          )
        }
      }

    readProgressRepository.save(progress)
    eventPublisher.publishEvent(DomainEvent.ReadProgressChanged(progress))
  }

  fun deleteBookFiles(book: Book) {
    if (book.path.notExists()) return logger.info { "Cannot delete book file, path does not exist: ${book.path}" }
    if (!book.path.isWritable()) return logger.info { "Cannot delete book file, path is not writable: ${book.path}" }

    val thumbnails =
      thumbnailBookRepository
        .findAllByBookIdAndType(book.id, setOf(ThumbnailBook.Type.SIDECAR))
        .mapNotNull { it.url?.toURI()?.toPath() }
        .filter { it.exists() && it.isWritable() }

    if (book.path.deleteIfExists()) {
      logger.info { "Deleted file: ${book.path}" }
      historicalEventRepository.insert(HistoricalEvent.BookFileDeleted(book, "File was deleted by user request"))
    }
    thumbnails.forEach {
      if (it.deleteIfExists()) logger.info { "Deleted file: $it" }
    }

    if (book.path.parent
        .listDirectoryEntries()
        .isEmpty()
    )
      if (book.path.parent.deleteIfExists()) {
        logger.info { "Deleted directory: ${book.path.parent}" }
        historicalEventRepository.insert(HistoricalEvent.SeriesFolderDeleted(book.seriesId, book.path.parent, "Folder was deleted because it was empty"))
      }

    softDeleteMany(listOf(book))
  }
}
