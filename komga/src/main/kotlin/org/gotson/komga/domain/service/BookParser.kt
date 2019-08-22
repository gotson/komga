package org.gotson.komga.domain.service

import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.path
import org.gotson.komga.infrastructure.archive.ContentDetector
import org.gotson.komga.infrastructure.archive.RarExtractor
import org.gotson.komga.infrastructure.archive.ZipExtractor
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class BookParser(
    private val contentDetector: ContentDetector,
    private val zipExtractor: ZipExtractor,
    private val rarExtractor: RarExtractor
) {

  val supportedMediaTypes = mapOf(
      "application/zip" to zipExtractor,
      "application/x-rar-compressed" to rarExtractor
  )

  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  private val thumbnailSize = 300
  private val thumbnailFormat = "png"

  fun parse(book: Book): BookMetadata {
    logger.info { "Trying to parse book: ${book.url}" }

    val mediaType = contentDetector.detectMediaType(book.path())
    logger.info { "Detected media type: $mediaType" }
    if (!supportedMediaTypes.keys.contains(mediaType))
      throw UnsupportedMediaTypeException("Unsupported mime type: $mediaType. File: ${book.url}", mediaType)

    val pages = supportedMediaTypes.getValue(mediaType).getPagesList(book.path())
        .sortedWith(compareBy(natSortComparator) { it.fileName })
    logger.info { "Book has ${pages.size} pages" }

    logger.info { "Trying to generate cover for book: ${book.url}" }
    val thumbnail = generateThumbnail(book, mediaType, pages.first().fileName)

    return BookMetadata(mediaType = mediaType, status = Status.READY, pages = pages, thumbnail = thumbnail)
  }

  fun regenerateThumbnail(book: Book): BookMetadata {
    logger.info { "Regenerate thumbnail for book: ${book.url}" }

    if (book.metadata.status != Status.READY) {
      logger.warn { "Book metadata is not ready, cannot generate thumbnail" }
      throw MetadataNotReadyException()
    }

    val thumbnail = generateThumbnail(book, book.metadata.mediaType!!, book.metadata.pages.first().fileName)

    return BookMetadata(
        mediaType = book.metadata.mediaType,
        status = Status.READY,
        pages = book.metadata.pages,
        thumbnail = thumbnail
    )
  }

  private fun generateThumbnail(book: Book, mediaType: String, entry: String): ByteArray? =
      try {
        ByteArrayOutputStream().use {
          supportedMediaTypes.getValue(mediaType).getPageStream(book.path(), entry).let { cover ->
            Thumbnails.of(cover.inputStream())
                .size(thumbnailSize, thumbnailSize)
                .outputFormat(thumbnailFormat)
                .toOutputStream(it)
            it.toByteArray()
          }
        }
      } catch (ex: Exception) {
        logger.warn(ex) { "Could not generate thumbnail for book: ${book.url}" }
        null
      }

  fun getPageContent(book: Book, number: Int): ByteArray {
    logger.info { "Get page #$number for book: ${book.url}" }

    if (book.metadata.status != Status.READY) {
      logger.warn { "Book metadata is not ready, cannot get pages" }
      throw MetadataNotReadyException()
    }

    if (number > book.metadata.pages.size || number <= 0) {
      logger.error { "Page number #$number is out of bounds. Book has ${book.metadata.pages.size} pages" }
      throw ArrayIndexOutOfBoundsException("Page $number does not exist")
    }

    return supportedMediaTypes.getValue(book.metadata.mediaType!!).getPageStream(book.path(), book.metadata.pages[number - 1].fileName)
  }
}

class MetadataNotReadyException : Exception()
class UnsupportedMediaTypeException(msg: String, val mediaType: String) : Exception(msg)