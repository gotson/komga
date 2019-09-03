package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.Status
import org.gotson.komga.domain.model.UnsupportedMediaTypeException
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
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

  @Throws(UnsupportedMediaTypeException::class)
  fun getBookPage(book: Book, number: Int, convertTo: ImageType? = null): BookPageContent {
    val pageContent = bookParser.getPageContent(book, number)
    val pageMediaType = book.metadata.pages[number - 1].mediaType

    convertTo?.let {
      val msg = "Convert page #$number of book $book from $pageMediaType to ${it.mediaType}"
      if (!imageConverter.supportedReadMediaTypes.contains(pageMediaType)) {
        throw UnsupportedMediaTypeException("$msg: unsupported read format $pageMediaType", pageMediaType)
      }
      if (!imageConverter.supportedWriteMediaTypes.contains(it.mediaType)) {
        throw UnsupportedMediaTypeException("$msg: unsupported cannot write format ${it.mediaType}", it.mediaType)
      }
      if (pageMediaType == it.mediaType) {
        logger.warn { "$msg: same format, no need for conversion" }
        return@let
      }

      logger.info { msg }
      val convertedPage = try {
        imageConverter.convertImage(pageContent, it.imageIOFormat)
      } catch (e: Exception) {
        logger.error(e) { "$msg: conversion failed" }
        throw e
      }
      return BookPageContent(number, convertedPage, it.mediaType)
    }

    return BookPageContent(number, pageContent, pageMediaType)
  }
}