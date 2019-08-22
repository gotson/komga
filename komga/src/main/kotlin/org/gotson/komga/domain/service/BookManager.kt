package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.persistence.BookRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.Future
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class BookManager(
    private val bookRepository: BookRepository,
    private val bookParser: BookParser
) {

  @Transactional
  @Async("parseBookTaskExecutor")
  fun parseAndPersist(book: Book): Future<Long> {
    logger.info { "Parse and persist book: ${book.url}" }
    return AsyncResult(measureTimeMillis {
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
    }.also { logger.info { "Parsing finished in ${DurationFormatUtils.formatDurationHMS(it)}" } })
  }

  @Transactional
  @Async("parseBookTaskExecutor")
  fun regenerateThumbnailAndPersist(book: Book): Future<Long> {
    logger.info { "Regenerate thumbnail and persist book: ${book.url}" }
    return AsyncResult(measureTimeMillis {
      try {
        book.metadata = bookParser.regenerateThumbnail(book)
      } catch (ex: Exception) {
        logger.error(ex) { "Error while recreating thumbnail" }
        book.metadata = BookMetadata(status = Status.ERROR)
      }
      bookRepository.save(book)
    }.also { logger.info { "Thumbnail generated in ${DurationFormatUtils.formatDurationHMS(it)}" } })
  }
}