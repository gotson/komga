package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.infrastructure.image.mediaTypeToImageIOFormat
import org.gotson.komga.infrastructure.image.toImageIOFormat
import org.gotson.komga.infrastructure.image.toMediaType
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
    private val bookParser: BookParser,
    private val imageConverter: ImageConverter
) {

  @Transactional
  @Async("parseBookTaskExecutor")
  fun parseAndPersist(book: Book): Future<Long> {
    logger.info { "Parse and persist book: $book" }
    return AsyncResult(measureTimeMillis {
      try {
        book.metadata = bookParser.parse(book)
      } catch (ex: UnsupportedMediaTypeException) {
        logger.info(ex) { "Unsupported media type: ${ex.mediaType}. Book: $book" }
        book.metadata = BookMetadata(status = Status.UNSUPPORTED, mediaType = ex.mediaType)
      } catch (ex: Exception) {
        logger.error(ex) { "Error while parsing. Book: $book" }
        book.metadata = BookMetadata(status = Status.ERROR)
      }
      bookRepository.save(book)
    }.also { logger.info { "Parsing finished in ${DurationFormatUtils.formatDurationHMS(it)}" } })
  }

  @Transactional
  @Async("parseBookTaskExecutor")
  fun regenerateThumbnailAndPersist(book: Book): Future<Long> {
    logger.info { "Regenerate thumbnail and persist book: $book" }
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

  fun getBookPage(book: Book, number: Int, convertTo: ImageType = ImageType.ORIGINAL): BookPageContent {
    val pageContent = bookParser.getPageContent(book, number)
    val pageMediaType = book.metadata.pages[number - 1].mediaType

    if (convertTo != ImageType.ORIGINAL) {
      val pageFormat = mediaTypeToImageIOFormat(pageMediaType)
      val convertFormat = convertTo.toImageIOFormat()
      if (pageFormat != null && convertFormat != null && imageConverter.canConvert(pageFormat, convertFormat)) {
        if (pageFormat != convertFormat) {
          try {
            logger.info { "Trying to convert page #$number of book $book from $pageFormat to $convertFormat" }
            val convertedPage = imageConverter.convertImage(pageContent, convertFormat)
            return BookPageContent(number, convertedPage, convertTo.toMediaType() ?: "application/octet-stream")
          } catch (ex: Exception) {
            logger.error(ex) { "Failed to convert page #$number of book $book to $convertFormat" }
          }
        } else {
          logger.warn { "Cannot convert page #$number of book $book from $pageFormat to $convertFormat: same format" }
        }
      } else {
        logger.warn { "Cannot convert page #$number of book $book to $convertFormat: unsupported format" }
      }
    }

    return BookPageContent(number, pageContent, pageMediaType)
  }
}