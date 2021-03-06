package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.SidecarRepository
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.language.notEquals
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Paths
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

@Service
class LibraryContentLifecycle(
  private val fileSystemScanner: FileSystemScanner,
  private val seriesRepository: SeriesRepository,
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val mediaRepository: MediaRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val collectionRepository: SeriesCollectionRepository,
  private val readListRepository: ReadListRepository,
  private val sidecarRepository: SidecarRepository,
  private val komgaProperties: KomgaProperties,
  private val taskReceiver: TaskReceiver,
  private val transactionTemplate: TransactionTemplate,
) {

  fun scanRootFolder(library: Library) {
    logger.info { "Updating library: $library" }
    measureTime {
      val scanResult = fileSystemScanner.scanRootFolder(Paths.get(library.root.toURI()), library.scanForceModifiedTime)
      val scannedSeries =
        scanResult
          .series
          .map { (series, books) ->
            series.copy(libraryId = library.id) to books.map { it.copy(libraryId = library.id) }
          }.toMap()

      // delete series that don't exist anymore
      if (scannedSeries.isEmpty()) {
        logger.info { "Scan returned no series, deleting all existing series" }
        val series = seriesRepository.findAllByLibraryId(library.id)
        seriesLifecycle.deleteMany(series)
      } else {
        scannedSeries.keys.map { it.url }.let { urls ->
          val series = seriesRepository.findAllByLibraryIdAndUrlNotIn(library.id, urls)
          if (series.isNotEmpty()) {
            logger.info { "Deleting series not on disk anymore: $series" }
            seriesLifecycle.deleteMany(series)
          }
        }
      }

      scannedSeries.forEach { (newSeries, newBooks) ->
        val existingSeries = seriesRepository.findByLibraryIdAndUrlOrNull(library.id, newSeries.url)

        // if series does not exist, save it
        if (existingSeries == null) {
          logger.info { "Adding new series: $newSeries" }
          val createdSeries = seriesLifecycle.createSeries(newSeries)
          seriesLifecycle.addBooks(createdSeries, newBooks)
          seriesLifecycle.sortBooks(createdSeries)
        } else {
          // if series already exists, update it
          logger.debug { "Scanned series already exists. Scanned: $newSeries, Existing: $existingSeries" }
          val seriesChanged = newSeries.fileLastModified.notEquals(existingSeries.fileLastModified)
          if (seriesChanged) {
            logger.info { "Series changed on disk, updating: $existingSeries" }
            seriesRepository.update(existingSeries.copy(fileLastModified = newSeries.fileLastModified))
          }
          if (library.scanDeep || seriesChanged) {
            // update list of books with existing entities if they exist
            val existingBooks = bookRepository.findAllBySeriesId(existingSeries.id)
            logger.debug { "Existing books: $existingBooks" }
            // update existing books
            newBooks.forEach { newBook ->
              logger.debug { "Trying to match scanned book by url: $newBook" }
              existingBooks.find { it.url == newBook.url }?.let { existingBook ->
                logger.debug { "Matched existing book: $existingBook" }
                if (newBook.fileLastModified.notEquals(existingBook.fileLastModified)) {
                  logger.info { "Book changed on disk, update and reset media status: $existingBook" }
                  val updatedBook = existingBook.copy(
                    fileLastModified = newBook.fileLastModified,
                    fileSize = newBook.fileSize
                  )
                  transactionTemplate.executeWithoutResult {
                    mediaRepository.findById(existingBook.id).let {
                      mediaRepository.update(it.copy(status = Media.Status.OUTDATED))
                    }
                    bookRepository.update(updatedBook)
                  }
                }
              }
            }

            // remove books not present anymore
            val newBooksUrls = newBooks.map { it.url }
            existingBooks
              .filterNot { existingBook -> newBooksUrls.contains(existingBook.url) }
              .let { books ->
                logger.info { "Deleting books not on disk anymore: $books" }
                bookLifecycle.deleteMany(books)
                books.map { it.seriesId }.distinct().forEach { taskReceiver.refreshSeriesMetadata(it) }
              }

            // add new books
            val existingBooksUrls = existingBooks.map { it.url }
            val booksToAdd = newBooks.filterNot { newBook -> existingBooksUrls.contains(newBook.url) }
            logger.info { "Adding new books: $booksToAdd" }
            seriesLifecycle.addBooks(existingSeries, booksToAdd)

            // sort all books
            seriesLifecycle.sortBooks(existingSeries)
          }
        }
      }

      val existingSidecars = sidecarRepository.findAll()
      scanResult.sidecars.forEach { newSidecar ->
        val existingSidecar = existingSidecars.firstOrNull { it.url == newSidecar.url }
        if (existingSidecar == null || existingSidecar.lastModifiedTime.notEquals(newSidecar.lastModifiedTime)) {
          when (newSidecar.source) {
            Sidecar.Source.SERIES ->
              seriesRepository.findByLibraryIdAndUrlOrNull(library.id, newSidecar.parentUrl)?.let { series ->
                logger.info { "Sidecar changed on disk (${newSidecar.url}, refresh Series for ${newSidecar.type}: $series" }
                when (newSidecar.type) {
                  Sidecar.Type.ARTWORK -> taskReceiver.refreshSeriesLocalArtwork(series.id)
                }
              }
            Sidecar.Source.BOOK ->
              bookRepository.findByLibraryIdAndUrlOrNull(library.id, newSidecar.parentUrl)?.let { book ->
                logger.info { "Sidecar changed on disk (${newSidecar.url}, refresh Book for ${newSidecar.type}: $book" }
                when (newSidecar.type) {
                  Sidecar.Type.ARTWORK -> taskReceiver.refreshBookLocalArtwork(book.id)
                }
              }
          }
          sidecarRepository.save(library.id, newSidecar)
        }
      }

      // cleanup sidecars that don't exist anymore
      scanResult.sidecars.map { it.url }.let { newSidecarsUrls ->
        existingSidecars
          .filterNot { existing -> newSidecarsUrls.contains(existing.url) }
          .let { sidecars ->
            sidecarRepository.deleteByLibraryIdAndUrls(library.id, sidecars.map { it.url })
          }
      }

      if (komgaProperties.deleteEmptyCollections) {
        logger.info { "Deleting empty collections" }
        collectionRepository.deleteEmpty()
      }

      if (komgaProperties.deleteEmptyReadLists) {
        logger.info { "Deleting empty read lists" }
        readListRepository.deleteEmpty()
      }
    }.also { logger.info { "Library updated in $it" } }
  }
}
