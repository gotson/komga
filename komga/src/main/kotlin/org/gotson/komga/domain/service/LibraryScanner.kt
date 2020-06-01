package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.springframework.stereotype.Service
import java.nio.file.Paths
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Service
class LibraryScanner(
  private val fileSystemScanner: FileSystemScanner,
  private val seriesRepository: SeriesRepository,
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val mediaRepository: MediaRepository,
  private val seriesLifecycle: SeriesLifecycle
) {

  fun scanRootFolder(library: Library) {
    logger.info { "Updating library: $library" }
    val scannedSeries =
      fileSystemScanner.scanRootFolder(Paths.get(library.root.toURI()))
        .map { (series, books) ->
          series.copy(libraryId = library.id) to books.map { it.copy(libraryId = library.id) }
        }.toMap()

    // delete series that don't exist anymore
    if (scannedSeries.isEmpty()) {
      logger.info { "Scan returned no series, deleting all existing series" }
      seriesRepository.findByLibraryId(library.id).forEach {
        seriesLifecycle.deleteSeries(it.id)
      }
    } else {
      scannedSeries.keys.map { it.url }.let { urls ->
        seriesRepository.findByLibraryIdAndUrlNotIn(library.id, urls).forEach {
          logger.info { "Deleting series not on disk anymore: $it" }
          seriesLifecycle.deleteSeries(it.id)
        }
      }
    }

    scannedSeries.forEach { (newSeries, newBooks) ->
      val existingSeries = seriesRepository.findByLibraryIdAndUrl(library.id, newSeries.url)

      // if series does not exist, save it
      if (existingSeries == null) {
        logger.info { "Adding new series: $newSeries" }
        val createdSeries = seriesLifecycle.createSeries(newSeries)
        seriesLifecycle.addBooks(createdSeries, newBooks)
        seriesLifecycle.sortBooks(createdSeries)
      } else {
        // if series already exists, update it
        if (newSeries.fileLastModified.truncatedTo(ChronoUnit.MILLIS) != existingSeries.fileLastModified.truncatedTo(ChronoUnit.MILLIS)) {
          logger.info { "Series changed on disk, updating: $existingSeries" }
          existingSeries.fileLastModified = newSeries.fileLastModified

          seriesRepository.update(existingSeries)

          // update list of books with existing entities if they exist
          val existingBooks = bookRepository.findBySeriesId(existingSeries.id)

          // update existing books
          newBooks.forEach { newBook ->
            existingBooks.find { it.url == newBook.url }?.let { existingBook ->
              if (newBook.fileLastModified.truncatedTo(ChronoUnit.MILLIS) != existingBook.fileLastModified.truncatedTo(ChronoUnit.MILLIS)) {
                logger.info { "Book changed on disk, update and reset media status: $existingBook" }
                val updatedBook = existingBook.copy(
                  fileLastModified = newBook.fileLastModified,
                  fileSize = newBook.fileSize
                )
                mediaRepository.findById(existingBook.id).let {
                  mediaRepository.update(it.reset())
                }
                bookRepository.update(updatedBook)
              }
            }
          }

          // remove books not present anymore
          existingBooks
            .filterNot { existingBook -> newBooks.map { it.url }.contains(existingBook.url) }
            .forEach { bookLifecycle.delete(it.id) }

          // add new books
          val booksToAdd = newBooks.filterNot { newBook -> existingBooks.map { it.url }.contains(newBook.url) }
          seriesLifecycle.addBooks(existingSeries, booksToAdd)

          // sort all books
          seriesLifecycle.sortBooks(existingSeries)
        }
      }
    }
  }

}
