package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.infrastructure.archive.ContentDetector
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private val logger = KotlinLogging.logger {}

@Service
class BookManager(
    private val bookRepository: BookRepository,
    private val bookParser: BookParser,
    private val contentDetector: ContentDetector
) {

  fun parseAndPersist(book: Book) {
    logger.info { "Parse and persist book: ${book.url}" }
    try {
      book.metadata = bookParser.parse(book)
    } catch (ex: UnsupportedMediaTypeException) {
      logger.info(ex) { "Unsupported media type: ${ex.mediaType}" }
      book.metadata = BookMetadata(status = Status.UNSUPPORTED, mediaType = ex.mediaType)
    } catch (ex: Exception) {
      logger.error(ex) { "Error while parsing" }
      book.metadata = BookMetadata(status = Status.ERROR)
    }
    bookRepository.save(book)
  }

  fun getPage(book: Book, number: Int): BookPage {
    logger.info { "Get page #$number for book: ${book.url}" }

    if (book.metadata.status == Status.UNKNOWN) {
      logger.info { "Book metadata is unknown, parsing it now" }
      parseAndPersist(book)
    }

    if (book.metadata.status != Status.READY) {
      logger.warn { "Book metadata is not ready, cannot get pages" }
      throw MetadataNotReadyException()
    }

    lateinit var mediaType: String
    lateinit var content: ByteArray

    bookParser.getPage(book, number).use { stream ->
      if (stream.markSupported()) {
        logger.debug { "Stream supports mark, passing it as is for content detection" }
        mediaType = contentDetector.detectMediaType(stream)
        content = stream.readBytes()
      } else {
        logger.debug { "Stream does not support mark, using a cloned stream for content detection" }
        val buffer = ByteArrayOutputStream()
        stream.copyTo(buffer)
        val clonedStream = ByteArrayInputStream(buffer.toByteArray())

        mediaType = clonedStream.use { contentDetector.detectMediaType(it) }
        content = buffer.toByteArray()
      }
    }

    logger.info { "Page media type: $mediaType" }

    return BookPage(mediaType, content)
  }
}