package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.BookRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class AsyncOrchestrator(
    private val libraryManager: LibraryManager,
    private val bookRepository: BookRepository,
    private val bookManager: BookManager
) {


  @Async("periodicScanTaskExecutor")
  fun scanAndParse(library: Library) {
    logger.info { "Starting periodic library scan" }
    libraryManager.scanRootFolder(library)

    logger.info { "Starting periodic book parsing" }
    libraryManager.parseUnparsedBooks()
  }

  @Async("regenerateThumbnailsTaskExecutor")
  fun regenerateAllThumbnails() {
    logger.info { "Regenerate thumbnail for all books" }
    generateThumbnails(bookRepository.findAll())
  }

  @Async("regenerateThumbnailsTaskExecutor")
  fun regenerateMissingThumbnails() {
    logger.info { "Regenerate missing thumbnails" }
    generateThumbnails(bookRepository.findAllByMetadataThumbnailIsNull())
  }

  private fun generateThumbnails(books: List<Book>) {
    var sumOfTasksTime = 0L
    measureTimeMillis {
      sumOfTasksTime = books
          .map { bookManager.regenerateThumbnailAndPersist(it) }
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
}