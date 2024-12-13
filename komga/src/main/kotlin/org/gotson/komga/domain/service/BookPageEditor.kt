package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookAction
import org.gotson.komga.domain.model.BookConversionException
import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.HistoricalEvent
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.model.restoreHashFrom
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.language.notEquals
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.Deflater
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream

private val logger = KotlinLogging.logger {}
private const val TEMP_PREFIX = "komga_page_removal_"
private const val TEMP_SUFFIX = ".tmp"

@Service
class BookPageEditor(
  private val bookAnalyzer: BookAnalyzer,
  private val fileSystemScanner: FileSystemScanner,
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val libraryRepository: LibraryRepository,
  private val pageHashRepository: PageHashRepository,
  private val transactionTemplate: TransactionTemplate,
  private val eventPublisher: ApplicationEventPublisher,
  private val historicalEventRepository: HistoricalEventRepository,
) {
  private val convertibleTypes = listOf(MediaType.ZIP.type)

  private val failedPageRemoval = mutableListOf<String>()

  fun removeHashedPages(
    book: Book,
    pagesToDelete: Collection<BookPageNumbered>,
  ): BookAction? {
    // perform various checks
    if (failedPageRemoval.contains(book.id)) {
      logger.info { "Book page removal already failed before, skipping" }
      return null
    }

    fileSystemScanner.scanFile(book.path)?.let { scannedBook ->
      if (scannedBook.fileLastModified.notEquals(book.fileLastModified)) {
        logger.info { "Book has changed on disk, skipping. Db: ${book.fileLastModified}. Scanned: ${scannedBook.fileLastModified}" }
        return null
      }
    } ?: throw FileNotFoundException("File not found: ${book.path}")

    val media = mediaRepository.findById(book.id)

    if (!convertibleTypes.contains(media.mediaType))
      throw MediaUnsupportedException("${media.mediaType} cannot be converted. Must be one of $convertibleTypes")

    if (media.status != Media.Status.READY)
      throw MediaNotReadyException()

    // create a temp file with the pages removed
    val pagesToKeep =
      media.pages.filterIndexed { index, page ->
        pagesToDelete.find { candidate ->
          candidate.fileHash == page.fileHash &&
            candidate.mediaType == page.mediaType &&
            candidate.fileName == page.fileName &&
            candidate.pageNumber == index + 1
        } == null
      }
    if (media.pages.size != (pagesToKeep.size + pagesToDelete.size)) {
      logger.info { "Should be removing ${pagesToDelete.size} pages from book, but count doesn't add up, skipping" }
      return null
    }

    logger.info { "Start removal of ${pagesToDelete.size} pages for book: $book" }
    logger.debug { "Pages: ${media.pages}" }
    logger.debug { "Pages to delete: $pagesToDelete" }
    logger.debug { "Pages to keep: $pagesToKeep" }

    val tempFile = File.createTempFile(TEMP_PREFIX, TEMP_SUFFIX, book.path.parent.toFile()).toPath()
    logger.info { "Creating new file: $tempFile" }
    ZipArchiveOutputStream(tempFile.outputStream()).use { zipStream ->
      zipStream.setMethod(ZipArchiveOutputStream.DEFLATED)
      zipStream.setLevel(Deflater.NO_COMPRESSION)

      pagesToKeep
        .map { it.fileName }
        .union(media.files.map { it.fileName })
        .forEach { entry ->
          zipStream.putArchiveEntry(ZipArchiveEntry(entry))
          zipStream.write(bookAnalyzer.getFileContent(BookWithMedia(book, media), entry))
          zipStream.closeArchiveEntry()
        }
    }

    // perform checks on new file
    val createdBook =
      fileSystemScanner
        .scanFile(tempFile)
        ?.copy(
          id = book.id,
          seriesId = book.seriesId,
          libraryId = book.libraryId,
        )
        ?: throw IllegalStateException("Newly created book could not be scanned: $tempFile")

    val createdMedia = bookAnalyzer.analyze(createdBook, libraryRepository.findById(book.libraryId).analyzeDimensions)

    try {
      when {
        createdMedia.status != Media.Status.READY
        -> throw BookConversionException("Created file could not be analyzed, aborting page removal")

        createdMedia.mediaType != MediaType.ZIP.type
        -> throw BookConversionException("Created file is not a zip file, aborting page removal")

        !createdMedia.pages
          .map { FilenameUtils.getName(it.fileName) to it.mediaType }
          .containsAll(pagesToKeep.map { FilenameUtils.getName(it.fileName) to it.mediaType })
        -> throw BookConversionException("Created file does not contain all pages to keep from existing file, aborting conversion")

        !createdMedia.files
          .map { FilenameUtils.getName(it.fileName) }
          .containsAll(media.files.map { FilenameUtils.getName(it.fileName) })
        -> throw BookConversionException("Created file does not contain all files from existing file, aborting page removal")
      }
    } catch (e: BookConversionException) {
      tempFile.deleteIfExists()
      failedPageRemoval += book.id
      throw e
    }

    tempFile.moveTo(book.path, true)
    val newBook =
      fileSystemScanner
        .scanFile(book.path)
        ?.copy(
          id = book.id,
          seriesId = book.seriesId,
          libraryId = book.libraryId,
        )
        ?: throw IllegalStateException("Newly created book could not be scanned after replacing existing one: ${book.path}")

    val mediaWithHashes = createdMedia.copy(pages = createdMedia.pages.restoreHashFrom(media.pages))

    transactionTemplate.executeWithoutResult {
      bookRepository.update(newBook)
      mediaRepository.update(mediaWithHashes)
      pagesToDelete
        .mapNotNull { pageHashRepository.findKnown(it.fileHash) }
        .forEach { pageHashRepository.update(it.copy(deleteCount = it.deleteCount + 1)) }
    }

    pagesToDelete.forEach { historicalEventRepository.insert(HistoricalEvent.DuplicatePageDeleted(book, it)) }
    eventPublisher.publishEvent(DomainEvent.BookUpdated(newBook))

    return if (pagesToDelete.any { it.pageNumber == 1 }) BookAction.GENERATE_THUMBNAIL else null
  }
}
