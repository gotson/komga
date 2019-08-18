package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.path
import org.gotson.komga.infrastructure.archive.ContentDetector
import org.gotson.komga.infrastructure.archive.RarExtractor
import org.gotson.komga.infrastructure.archive.ZipExtractor
import org.springframework.stereotype.Service
import java.io.InputStream

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

  fun parse(book: Book): BookMetadata {
    logger.info { "Trying to parse book: ${book.url}" }

    val mediaType = contentDetector.detectMediaType(book.path())
    logger.info { "Detected media type: $mediaType" }
    if (!supportedMediaTypes.keys.contains(mediaType))
      throw UnsupportedMediaTypeException("Unsupported mime type: $mediaType. File: ${book.url}", mediaType)

    val pages = supportedMediaTypes.getValue(mediaType).getPagesList(book.path())
    logger.info { "Book has ${pages.size} pages" }

    return BookMetadata(mediaType = mediaType, status = Status.READY, pages = pages)
  }

  fun getPageStream(book: Book, number: Int): InputStream {
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