package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookPageContent
import org.gotson.komga.domain.model.ImageConversionException
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaNotReadyException
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class BookLifecycle(
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val bookAnalyzer: BookAnalyzer,
  private val imageConverter: ImageConverter
) {

  fun analyzeAndPersist(book: Book) {
    logger.info { "Analyze and persist book: $book" }
    val media = try {
      bookAnalyzer.analyze(book)
    } catch (ex: Exception) {
      logger.error(ex) { "Error while analyzing book: $book" }
      Media(status = Media.Status.ERROR, comment = ex.message)
    }.copy(bookId = book.id)
    mediaRepository.update(media)
  }

  fun regenerateThumbnailAndPersist(book: Book) {
    logger.info { "Regenerate thumbnail and persist book: $book" }
    val media = try {
      bookAnalyzer.regenerateThumbnail(book)
    } catch (ex: Exception) {
      logger.error(ex) { "Error while recreating thumbnail" }
      Media(status = Media.Status.ERROR)
    }.copy(bookId = book.id)
    mediaRepository.update(media)
  }

  @Throws(
    ImageConversionException::class,
    MediaNotReadyException::class,
    IndexOutOfBoundsException::class
  )
  fun getBookPage(book: Book, number: Int, convertTo: ImageType? = null, resizeTo: Int? = null): BookPageContent {
    val media = mediaRepository.findById(book.id)
    val pageContent = bookAnalyzer.getPageContent(book, number)
    val pageMediaType = media.pages[number - 1].mediaType

    if (resizeTo != null) {
      val targetFormat = ImageType.JPEG
      val convertedPage = try {
        imageConverter.resizeImage(pageContent, targetFormat.imageIOFormat, resizeTo)
      } catch (e: Exception) {
        logger.error(e) { "Resize page #$number of book $book to $resizeTo: failed" }
        throw e
      }
      return BookPageContent(number, convertedPage, targetFormat.mediaType)
    } else {
      convertTo?.let {
        val msg = "Convert page #$number of book $book from $pageMediaType to ${it.mediaType}"
        if (!imageConverter.supportedReadMediaTypes.contains(pageMediaType)) {
          throw ImageConversionException("$msg: unsupported read format $pageMediaType")
        }
        if (!imageConverter.supportedWriteMediaTypes.contains(it.mediaType)) {
          throw ImageConversionException("$msg: unsupported write format ${it.mediaType}")
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

  fun delete(bookId: Long) {
    logger.info { "Delete book id: $bookId" }

    mediaRepository.delete(bookId)
    bookMetadataRepository.delete(bookId)

    bookRepository.delete(bookId)
  }
}
