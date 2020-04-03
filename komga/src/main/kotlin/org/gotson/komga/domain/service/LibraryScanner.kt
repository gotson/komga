package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.apache.commons.lang3.time.DurationFormatUtils
import org.gotson.komga.application.service.BookLifecycle
import org.gotson.komga.application.service.MetadataLifecycle
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths
import java.time.temporal.ChronoUnit
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class LibraryScanner(
    private val fileSystemScanner: FileSystemScanner,
    private val seriesRepository: SeriesRepository,
    private val bookRepository: BookRepository,
    private val bookLifecycle: BookLifecycle,
    private val metadataLifecycle: MetadataLifecycle
) {

  @Transactional
  fun scanRootFolder(library: Library) {
    logger.info { "Updating library: ${library.name}, root folder: ${library.root}" }
    measureTimeMillis {
      val scannedSeries = fileSystemScanner.scanRootFolder(Paths.get(library.root.toURI()))

      // delete series that don't exist anymore
      if (scannedSeries.isEmpty()) {
        logger.info { "Scan returned no series, deleting all existing series" }
        seriesRepository.deleteByLibraryId(library.id)
      } else {
        scannedSeries.map { it.url }.let { urls ->
          seriesRepository.findByLibraryIdAndUrlNotIn(library.id, urls).forEach {
            logger.info { "Deleting series not on disk anymore: $it" }
            seriesRepository.delete(it)
          }
        }
      }

      scannedSeries.forEach { newSeries ->
        val existingSeries = seriesRepository.findByLibraryIdAndUrl(library.id, newSeries.url)

        // if series does not exist, save it
        if (existingSeries == null) {
          logger.info { "Adding new series: $newSeries" }
          seriesRepository.save(newSeries.also { it.library = library })
        } else {
          // if series already exists, update it
          if (newSeries.fileLastModified.truncatedTo(ChronoUnit.MILLIS) != existingSeries.fileLastModified.truncatedTo(ChronoUnit.MILLIS)) {
            logger.info { "Series changed on disk, updating: $newSeries" }
            existingSeries.fileLastModified = newSeries.fileLastModified

            // update list of books with existing entities if they exist
            existingSeries.books = newSeries.books.map { newBook ->
              val existingBook = bookRepository.findByUrl(newBook.url) ?: newBook

              if (newBook.fileLastModified.truncatedTo(ChronoUnit.MILLIS) != existingBook.fileLastModified.truncatedTo(ChronoUnit.MILLIS)) {
                logger.info { "Book changed on disk, update and reset media status: $newBook" }
                existingBook.fileLastModified = newBook.fileLastModified
                existingBook.fileSize = newBook.fileSize
                existingBook.media.reset()
              }
              existingBook
            }.toMutableList()

            seriesRepository.save(existingSeries)
          }
        }
      }
    }.also { logger.info { "Library update finished in ${DurationFormatUtils.formatDurationHMS(it)}" } }
  }

  fun analyzeUnknownBooks() {
    logger.info { "Analyze all books in status: unknown" }
    val booksToAnalyze = bookRepository.findAllByMediaStatus(Media.Status.UNKNOWN)

    var sumOfTasksTime = 0L
    measureTimeMillis {
      sumOfTasksTime = booksToAnalyze
          .map { bookLifecycle.analyzeAndPersist(it) }
          .map {
            try {
              it.get()
            } catch (ex: Exception) {
              0L
            }
          }
          .sum()
    }.also {
      logger.info { "Analyzed ${booksToAnalyze.size} books in ${DurationFormatUtils.formatDurationHMS(it)} (virtual: ${DurationFormatUtils.formatDurationHMS(sumOfTasksTime)})" }
    }

    logger.info { "Refresh metadata for all books analyzed" }
    booksToAnalyze.forEach {
      metadataLifecycle.refreshMetadata(it)
    }
  }
}
