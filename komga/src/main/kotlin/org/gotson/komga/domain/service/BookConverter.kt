package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookConversionException
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.HistoricalEvent
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.model.restoreHashFrom
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.language.notEquals
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.io.FileNotFoundException
import java.nio.file.FileAlreadyExistsException
import java.util.zip.Deflater
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.moveTo
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.outputStream

private val logger = KotlinLogging.logger {}

const val CBZ_EXTENSION = "cbz"

@Service
class BookConverter(
  private val bookAnalyzer: BookAnalyzer,
  private val fileSystemScanner: FileSystemScanner,
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val libraryRepository: LibraryRepository,
  private val transactionTemplate: TransactionTemplate,
  private val eventPublisher: ApplicationEventPublisher,
  private val historicalEventRepository: HistoricalEventRepository,
) {
  private val convertibleTypes = listOf(MediaType.RAR_4.type, MediaType.RAR_5.type)

  private val mediaTypeToExtension =
    listOf(MediaType.RAR_4, MediaType.RAR_5, MediaType.ZIP, MediaType.PDF, MediaType.EPUB)
      .associate { it.type to it.fileExtension }

  private val failedConversions = mutableListOf<String>()
  private val skippedRepairs = mutableListOf<String>()

  fun getConvertibleBooks(library: Library): Collection<Book> =
    if (library.convertToCbz) {
      bookRepository
        .findAllByLibraryIdAndMediaTypes(library.id, convertibleTypes)
        .also { logger.info { "Found ${it.size} books to convert" } }
    } else {
      logger.info { "CBZ conversion is not enabled, skipping" }
      emptyList()
    }

  fun convertToCbz(book: Book) {
    // perform various checks
    if (!libraryRepository.findById(book.libraryId).convertToCbz)
      return logger.info { "Book conversion is disabled for the library, it may have changed since the task was submitted, skipping" }

    if (failedConversions.contains(book.id))
      return logger.info { "Book conversion already failed before, skipping" }

    fileSystemScanner.scanFile(book.path)?.let { scannedBook ->
      if (scannedBook.fileLastModified.notEquals(book.fileLastModified))
        return logger.info { "Book has changed on disk, skipping" }
    } ?: throw FileNotFoundException("File not found: ${book.path}")

    val media = mediaRepository.findById(book.id)

    if (!convertibleTypes.contains(media.mediaType))
      throw MediaUnsupportedException("${media.mediaType} cannot be converted. Must be one of $convertibleTypes")

    if (media.status != Media.Status.READY)
      throw MediaNotReadyException()

    // perform conversion
    val destinationFilename = "${book.path.nameWithoutExtension}.$CBZ_EXTENSION"
    val destinationPath = book.path.parent.resolve(destinationFilename)
    if (destinationPath.exists())
      throw FileAlreadyExistsException("Destination file already exists: $destinationPath")

    logger.info { "Copying archive content to $destinationPath" }
    ZipArchiveOutputStream(destinationPath.outputStream()).use { zipStream ->
      zipStream.setMethod(ZipArchiveOutputStream.DEFLATED)
      zipStream.setLevel(Deflater.NO_COMPRESSION)

      media
        .pages
        .map { it.fileName }
        .union(media.files.map { it.fileName })
        .forEach { entry ->
          zipStream.putArchiveEntry(ZipArchiveEntry(entry))
          zipStream.write(bookAnalyzer.getFileContent(BookWithMedia(book, media), entry))
          zipStream.closeArchiveEntry()
        }
    }

    // perform checks on new file
    val convertedBook =
      fileSystemScanner
        .scanFile(destinationPath)
        ?.copy(
          id = book.id,
          seriesId = book.seriesId,
          libraryId = book.libraryId,
        )
        ?: throw IllegalStateException("Newly converted book could not be scanned: $destinationFilename")

    val convertedMedia = bookAnalyzer.analyze(convertedBook, libraryRepository.findById(book.libraryId).analyzeDimensions)

    try {
      when {
        convertedMedia.status != Media.Status.READY
        -> throw BookConversionException("Converted file could not be analyzed, aborting conversion")

        convertedMedia.mediaType != MediaType.ZIP.type
        -> throw BookConversionException("Converted file is not a zip file, aborting conversion")

        !convertedMedia.pages
          .map { FilenameUtils.getName(it.fileName) to it.mediaType }
          .containsAll(media.pages.map { FilenameUtils.getName(it.fileName) to it.mediaType })
        -> throw BookConversionException("Converted file does not contain all pages from existing file, aborting conversion")

        !convertedMedia.files
          .map { FilenameUtils.getName(it.fileName) }
          .containsAll(media.files.map { FilenameUtils.getName(it.fileName) })
        -> throw BookConversionException("Converted file does not contain all files from existing file, aborting conversion")
      }
    } catch (e: BookConversionException) {
      destinationPath.deleteIfExists()
      failedConversions += book.id
      throw e
    }

    if (book.path.deleteIfExists()) {
      logger.info { "Deleted old file: ${book.path}" }
      historicalEventRepository.insert(HistoricalEvent.BookFileDeleted(book, "File was deleted after conversion to CBZ"))
    }

    val mediaWithHashes = convertedMedia.copy(pages = convertedMedia.pages.restoreHashFrom(media.pages))

    transactionTemplate.executeWithoutResult {
      bookRepository.update(convertedBook)
      mediaRepository.update(mediaWithHashes)
    }

    historicalEventRepository.insert(HistoricalEvent.BookConverted(convertedBook, book))
    eventPublisher.publishEvent(DomainEvent.BookUpdated(convertedBook))
  }

  fun getMismatchedExtensionBooks(library: Library): Collection<Book> =
    mediaTypeToExtension.flatMap { (mediaType, extension) ->
      bookRepository.findAllByLibraryIdAndMismatchedExtension(library.id, mediaType, extension)
    }

  fun repairExtension(book: Book) {
    if (!libraryRepository.findById(book.libraryId).repairExtensions)
      return logger.info { "Repair extensions is disabled for the library, it may have changed since the task was submitted, skipping" }

    if (skippedRepairs.contains(book.id))
      return logger.info { "Extension repair has already been skipped before, skipping" }

    if (book.path.notExists()) throw FileNotFoundException("File not found: ${book.path}")

    val media = mediaRepository.findById(book.id)

    if (!mediaTypeToExtension.keys.contains(media.mediaType))
      throw MediaUnsupportedException("${media.mediaType} cannot be repaired. Must be one of ${mediaTypeToExtension.keys}")

    if (book.path.extension.lowercase() == "epub" && media.mediaType == MediaType.ZIP.type) {
      skippedRepairs += book.id
      logger.info { "EPUB file detected as zip should not be repaired, skipping: ${book.path}" }
      return
    }

    val actualExtension = book.path.extension
    val correctExtension = mediaTypeToExtension[media.mediaType]

    if (correctExtension == actualExtension) {
      logger.info { "MediaType (${media.mediaType}) and extension ($actualExtension) already match, skipping" }
      skippedRepairs += book.id
    }

    val destinationFilename = "${book.path.nameWithoutExtension}.$correctExtension"
    val destinationPath = book.path.parent.resolve(destinationFilename)
    if (destinationPath.exists())
      throw FileAlreadyExistsException("Destination file already exists: $destinationPath")

    logger.info { "Renaming ${book.path} to $destinationPath" }
    book.path.moveTo(destinationPath)

    val repairedBook =
      fileSystemScanner
        .scanFile(destinationPath)
        ?.copy(
          id = book.id,
          seriesId = book.seriesId,
          libraryId = book.libraryId,
        )
        ?: throw IllegalStateException("Repaired book could not be scanned: $destinationFilename")

    bookRepository.update(repairedBook)
  }
}
