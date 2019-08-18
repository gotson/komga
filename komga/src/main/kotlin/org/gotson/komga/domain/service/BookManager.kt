package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.persistence.BookRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class BookManager(
    private val bookRepository: BookRepository,
    private val bookParser: BookParser
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

}