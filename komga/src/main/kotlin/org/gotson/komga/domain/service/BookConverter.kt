package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookConversionException
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
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
) {

  private val convertibleTypes = listOf(MediaType.RAR_4.value)

  private val mediaTypeToExtension =
    listOf(MediaType.RAR_4, MediaType.ZIP, MediaType.PDF, MediaType.EPUB)
      .associate { it.value to it.fileExtension }

  private val failedConversions = mutableListOf<String>()
  private val skippedRepairs = mutableListOf<String>()

  fun getConvertibleBooks(library: Library): Collection<Book> =
    bookRepository.findAllByLibraryIdAndMediaTypes(library.id, convertibleTypes)

  fun convertToCbz(book: Book) {
    // TODO: check if file has changed on disk before doing conversion
    if (!libraryRepository.findById(book.libraryId).convertToCbz)
      return logger.info { "Book conversion is disabled for the library, it may have changed since the task was submitted, skipping" }

    if (failedConversions.contains(book.id))
      return logger.info { "Book conversion already failed before, skipping" }

    if (book.path.notExists()) throw FileNotFoundException("File not found: ${book.path}")

    val media = mediaRepository.findById(book.id)

    if (!convertibleTypes.contains(media.mediaType))
      throw MediaUnsupportedException("${media.mediaType} cannot be converted. Must be one of $convertibleTypes")
    if (media.status != Media.Status.READY)
      throw MediaNotReadyException()

    val destinationFilename = "${book.path.nameWithoutExtension}.$CBZ_EXTENSION"
    val destinationPath = book.path.parent.resolve(destinationFilename)
    if (destinationPath.exists())
      throw FileAlreadyExistsException("Destination file already exists: $destinationPath")

    logger.info { "Copying archive content to $destinationPath" }
    ZipArchiveOutputStream(destinationPath.outputStream()).use { zipStream ->
      zipStream.setMethod(ZipArchiveOutputStream.DEFLATED)
      zipStream.setLevel(Deflater.NO_COMPRESSION)

      media
        .pages.map { it.fileName }
        .union(media.files)
        .forEach { entry ->
          zipStream.putArchiveEntry(ZipArchiveEntry(entry))
          zipStream.write(bookAnalyzer.getFileContent(BookWithMedia(book, media), entry))
          zipStream.closeArchiveEntry()
        }
    }

    val convertedBook = fileSystemScanner.scanFile(destinationPath)
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

        convertedMedia.mediaType != MediaType.ZIP.value
        -> throw BookConversionException("Converted file is not a zip file, aborting conversion")

        !convertedMedia.pages.map { FilenameUtils.getName(it.fileName) to it.mediaType }
          .containsAll(media.pages.map { FilenameUtils.getName(it.fileName) to it.mediaType })
        -> throw BookConversionException("Converted file does not contain all pages from existing file, aborting conversion")

        !convertedMedia.files.map { FilenameUtils.getName(it) }
          .containsAll(media.files.map { FilenameUtils.getName(it) })
        -> throw BookConversionException("Converted file does not contain all files from existing file, aborting conversion")
      }
    } catch (e: BookConversionException) {
      destinationPath.deleteIfExists()
      failedConversions += book.id
      throw e
    }

    if (book.path.deleteIfExists())
      logger.info { "Deleted converted file: ${book.path}" }

    transactionTemplate.executeWithoutResult {
      bookRepository.update(convertedBook)
      // TODO: restore page hash from existing media
      mediaRepository.update(convertedMedia)
    }
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

    val repairedBook = fileSystemScanner.scanFile(destinationPath)
      ?.copy(
        id = book.id,
        seriesId = book.seriesId,
        libraryId = book.libraryId,
      )
      ?: throw IllegalStateException("Repaired book could not be scanned: $destinationFilename")

    bookRepository.update(repairedBook)
  }
}
