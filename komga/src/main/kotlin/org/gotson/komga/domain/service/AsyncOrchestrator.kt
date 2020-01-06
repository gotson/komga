package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class AsyncOrchestrator(
  private val libraryScanner: LibraryScanner,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle
) {

  @Async("periodicScanTaskExecutor")
  fun scanAndAnalyze() {
    logger.info { "Starting periodic libraries scan" }
    val libraries = libraryRepository.findAll()

    if (libraries.isEmpty()) {
      logger.info { "No libraries defined, nothing to scan" }
    } else {
      libraries.forEach {
        libraryScanner.scanRootFolder(it)
      }

      logger.info { "Starting periodic book parsing" }
      libraryScanner.analyzeUnknownBooks()
    }
  }

  @Async("regenerateThumbnailsTaskExecutor")
  fun regenerateAllThumbnails() {
    logger.info { "Regenerate thumbnail for all books" }
    generateThumbnails(bookRepository.findAll())
  }

  @Async("regenerateThumbnailsTaskExecutor")
  fun regenerateMissingThumbnails() {
    logger.info { "Regenerate missing thumbnails" }
    generateThumbnails(bookRepository.findAllByMediaThumbnailIsNull())
  }

  private fun generateThumbnails(books: List<Book>) {
    var sumOfTasksTime = 0L
    measureTimeMillis {
      sumOfTasksTime = books
        .map { bookLifecycle.regenerateThumbnailAndPersist(it) }
        .map {
          try {
            it.get()
          } catch (ex: Exception) {
            0L
          }
        }
        .sum()
    }.also {
      logger.info { "Generated ${books.size} thumbnails in ${DurationFormatUtils.formatDurationHMS(it)} (virtual: ${DurationFormatUtils.formatDurationHMS(sumOfTasksTime)})" }
    }
  }

  @Async("reAnalyzeBooksTaskExecutor")
  @Transactional
  fun reAnalyzeBooks(books: List<Book>) {
    val loadedBooks = bookRepository.findAllById(books.map { it.id })
    loadedBooks.forEach { it.media.reset() }
    bookRepository.saveAll(loadedBooks)

    loadedBooks.map { bookLifecycle.analyzeAndPersist(it) }
  }
}
