package org.gotson.komga.application.service

import mu.KotlinLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.LibraryScanner
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
  private val bookLifecycle: BookLifecycle,
  private val seriesRepository: SeriesRepository,
  private val metadataLifecycle: MetadataLifecycle
) {

  @Async("periodicScanTaskExecutor")
  fun scanAndAnalyzeAllLibraries() {
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

  @Async("periodicScanTaskExecutor")
  fun scanAndAnalyzeOneLibrary(library: Library) {
    libraryScanner.scanRootFolder(library)
    libraryScanner.analyzeUnknownBooks()
  }

  @Async("regenerateThumbnailsTaskExecutor")
  @Transactional
  fun generateThumbnails(books: List<Book>) {
    val loadedBooks = bookRepository.findAllById(books.map { it.id })
    var sumOfTasksTime = 0L
    measureTimeMillis {
      sumOfTasksTime = loadedBooks
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
      logger.info { "Generated ${loadedBooks.size} thumbnails in ${DurationFormatUtils.formatDurationHMS(it)} (virtual: ${DurationFormatUtils.formatDurationHMS(sumOfTasksTime)})" }
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

  @Async("reRefreshMetadataTaskExecutor")
  @Transactional
  fun refreshBooksMetadata(books: List<Book>) {
    bookRepository
      .findAllById(books.map { it.id })
      .forEach { metadataLifecycle.refreshMetadata(it) }
  }
}
