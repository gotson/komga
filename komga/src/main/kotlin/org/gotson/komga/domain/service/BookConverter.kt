package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.tika.mime.MediaType
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookConversionException
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.nio.file.FileAlreadyExistsException
import java.util.zip.Deflater
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
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
) {

  val convertibleTypes = listOf("application/x-rar-compressed; version=4")

  private val exclude = mutableListOf<String>()

  fun convertToCbz(book: Book) {
    if (!libraryRepository.findById(book.libraryId).convertToCbz)
      return logger.info { "Book conversion is disabled for the library, it may have changed since the task was submitted, skipping" }

    if (exclude.contains(book.id))
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
        libraryId = book.libraryId
      )
      ?: throw IllegalStateException("Newly converted book could not be scanned: $destinationFilename")

    val convertedMedia = bookAnalyzer.analyze(convertedBook)

    if (convertedMedia.status != Media.Status.READY ||
      convertedMedia.mediaType != MediaType.APPLICATION_ZIP.toString() ||
      !convertedMedia.pages.containsAll(media.pages) ||
      !convertedMedia.files.containsAll(media.files)
    ) {
      destinationPath.deleteIfExists()
      exclude += book.id
      throw BookConversionException("Converted file does not match existing file, aborting conversion")
    }

    if (book.path.deleteIfExists())
      logger.info { "Deleted converted file: ${book.path}" }

    bookRepository.update(convertedBook)
    mediaRepository.update(convertedMedia)
  }
}
