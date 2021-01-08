package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.file.FileHasher
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

@Service
class LibraryScanner(
  private val fileSystemScanner: FileSystemScanner,
  private val seriesRepository: SeriesRepository,
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val mediaRepository: MediaRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val fileHasher: FileHasher
) {

  fun scanRootFolder(library: Library) {
    logger.info { "Updating library: $library" }
    measureTime {
      val scannedSeries =
        fileSystemScanner.scanRootFolder(Paths.get(library.root.toURI()), library.scanForceModifiedTime)
          .map { (series, books) ->
            series.copy(libraryId = library.id) to
              books.map { it.copy(libraryId = library.id, fileHash = getBookHash(it)) }
          }
          .toMap()

      deleteNoLongerExistingSeries(scannedSeries, library)
      restoreDeletedSeries(scannedSeries)
      updateSeriesAndBooks(scannedSeries, library)
      restoreDeletedBooksAndAddNew(scannedSeries, library)
    }.also { logger.info { "Library updated in $it" } }
  }

  private fun getBookHash(book: Book): String {
    return bookRepository.findByLibraryIdAndUrlIncludeDeleted(book.libraryId, book.url)?.let {
      if (isModified(it.fileLastModified, book.fileLastModified)) fileHasher.getHash(book.path())
      else it.fileHash
    } ?: fileHasher.getHash(book.path())
  }

  private fun deleteNoLongerExistingSeries(scannedSeries: Map<Series, Collection<Book>>, library: Library) {
    if (scannedSeries.isEmpty()) {
      logger.info { "Scan returned no series, mark all existing series as deleted" }
      seriesRepository.findByLibraryId(library.id)
        .map { it.id }
        .let { seriesLifecycle.softDeleteMany(it) }
    } else {
      scannedSeries.keys.map { it.url }.let { urls ->
        val series = seriesRepository.findByLibraryIdAndUrlNotIn(library.id, urls).filterNot { it.deleted }
        if (series.isNotEmpty()) {
          logger.info { "Series is not on disk anymore, marking as deleted: $series" }
          seriesLifecycle.softDeleteMany(series.map { it.id })
        }
      }
    }
  }

  private fun updateSeriesAndBooks(scannedSeries: Map<Series, Collection<Book>>, library: Library) {
    scannedSeries.forEach { (newSeries, newBooks) ->
      val existingSeries = seriesRepository.findByLibraryIdAndUrlIncludeDeleted(library.id, newSeries.url)

      // if series does not exist, save it
      if (existingSeries == null) {
        logger.info { "Adding new series: $newSeries" }
        seriesLifecycle.createSeries(newSeries)
      } else {
        // if series already exists, update it
        logger.debug { "Scanned series already exists. Scanned: $newSeries, Existing: $existingSeries" }
        if (isModified(newSeries.fileLastModified, existingSeries.fileLastModified)) {
          logger.info { "Series changed on disk, updating: $existingSeries" }
          seriesRepository.update(existingSeries.copy(fileLastModified = newSeries.fileLastModified))
        }
        if (library.scanDeep || isModified(newSeries.fileLastModified, existingSeries.fileLastModified)) {
          val existingBooks = bookRepository.findBySeriesId(existingSeries.id)
          logger.debug { "Existing books: $existingBooks" }
          updateExistingBook(newBooks, existingBooks)
          deleteNoLongerExistingBooks(newBooks, existingBooks)
        }
      }
    }
  }

  private fun restoreDeletedBooksAndAddNew(scannedSeries: Map<Series, Collection<Book>>, library: Library) {
    scannedSeries.forEach { (newSeries, newBooks) ->
      seriesRepository.findByLibraryIdAndUrlIncludeDeleted(library.id, newSeries.url)?.let { existingSeries ->
        val existingBooks = bookRepository.findBySeriesId(existingSeries.id)

        val booksToCheck = newBooks.filterNot { newBook -> existingBooks.map { it.url }.contains(newBook.url) }
        val restoredBooks = restoreDeletedBooks(existingSeries, existingBooks, newBooks)
        val booksToAdd = booksToCheck.filterNot { toCheck ->
          restoredBooks.map { it.fileHash }.contains(toCheck.fileHash)
        }
        logger.info { "Adding new books: $booksToAdd" }
        seriesLifecycle.addBooks(existingSeries, booksToAdd)

        seriesLifecycle.sortBooks(existingSeries)
      }
    }
  }

  private fun restoreDeletedSeries(scannedSeries: Map<Series, Collection<Book>>) {
    scannedSeries
      //do checks only for new or deleted series
      .filterNot { seriesRepository.existsByLibraryIdAndUrl(it.key.libraryId, it.key.url) }
      .forEach { (newSeries, newBooks) ->
        val newHashes = newBooks.map { it.fileHash }
        var matchedSeries: Collection<Series> =
          seriesRepository.findByHashesInIncludeDeleted(newHashes)
            //check if series folder exists in case it was deleted but analysis wasn't done yet
            .filter { it.deleted || Files.notExists(it.path()) }
        if (matchedSeries.size > 1) {
          logger.warn { "matched multiple series: ${matchedSeries.map { it.path() }}, limiting matching to the library" }
          matchedSeries = seriesRepository.findByLibraryIdAndHashesInIncludeDeleted(newSeries.libraryId, newHashes)
          if (matchedSeries.size > 1) {
            logger.warn { "matched multiple series with similar books: ${matchedSeries.map { it.path() }}. Series will be added as a new one" }
            return
          }
        }

        if (matchedSeries.size == 1) {
          val seriesToRestore = matchedSeries.first()
          logger.info { "new series contains all books from previously deleted series. Restoring old series: ${seriesToRestore.path()}" }

          val deletedBooks: Collection<Book> = bookRepository.findBySeriesId(seriesToRestore.id)

          val toRestore: List<Book> = newBooks.mapNotNull { newBook ->
            deletedBooks.find { Pair(it.fileHash, it.fileSize) == Pair(newBook.fileHash, newBook.fileSize) }?.let {
              newBook.copy(id = it.id, seriesId = it.seriesId, libraryId = it.libraryId)
            }
          }
          bookLifecycle.restoreMany(toRestore)
          seriesLifecycle.restore(newSeries.copy(id = seriesToRestore.id, createdDate = seriesToRestore.createdDate))
        }
      }
  }

  private fun restoreDeletedBooks(
    existingSeries: Series,
    existingBooks: Collection<Book>,
    booksToCheck: Collection<Book>
  ): Collection<Book> {
    val booksToRestore = booksToCheck.asSequence().map { bookToCheck ->
      val matchedBooks = matchExistingBooksByHash(bookToCheck, existingSeries)
      Pair(bookToCheck, matchedBooks)
    }
      .filter { e ->
        if (e.second.size > 1) logger.warn { "Multiple matches: ${e.second}, skipping entry" }
        e.second.size == 1
      }
      .map { it.first to it.second.first() }
      .filterNot { newBook -> existingBooks.map { it.fileHash }.contains(newBook.first.fileHash) }
      .map { p -> p.first.copy(id = p.second.id, seriesId = existingSeries.id, deleted = false) }.toList()
    logger.info { "Restoring deleted books: $booksToRestore" }
    bookLifecycle.restoreMany(booksToRestore)
    return booksToRestore
  }

  private fun updateExistingBook(newBooks: Collection<Book>, existingBooks: Collection<Book>) {
    newBooks.forEach { newBook ->
      // update list of books with existing entities if they exist
      logger.debug { "Trying to match scanned book by url: $newBook" }
      existingBooks.find { it.url == newBook.url }?.let { existingBook ->
        logger.debug { "Matched existing book: $existingBook" }

        if (isModified(newBook.fileLastModified, existingBook.fileLastModified)) {
          logger.info { "Book changed on disk, update and reset media status: $existingBook" }
          val updatedBook = existingBook.copy(
            fileLastModified = newBook.fileLastModified,
            fileSize = newBook.fileSize,
            fileHash = newBook.fileHash
          )
          mediaRepository.findById(existingBook.id).let {
            mediaRepository.update(it.copy(status = Media.Status.OUTDATED))
          }
          bookRepository.update(updatedBook)
        }
      }
    }
  }

  private fun deleteNoLongerExistingBooks(newBooks: Collection<Book>, existingBooks: Collection<Book>) {
    val newBooksUrls = newBooks.map { it.url }
    existingBooks.filterNot { existingBook -> newBooksUrls.contains(existingBook.url) }
      .let { books ->
        logger.info { "Deleting books not on disk anymore: $books" }
        bookLifecycle.softDeleteMany(books.map { it.id })
      }
  }

  private fun matchExistingBooksByHash(bookToCheck: Book, existingSeries: Series): Collection<Book> {
    var existingBooks: Collection<Book> =
      bookRepository.findByFileHashAndSizeIncludeDeleted(bookToCheck.fileHash, bookToCheck.fileSize)
        //check if book exists in case it was deleted but library analysis wasn't done yet
        .filter { b -> b.deleted || Files.notExists(b.path()) }
    if (existingBooks.size > 1) {
      logger.warn { "Multiple matches: ${existingBooks}, limiting matching to library of the series" }
      existingBooks = bookRepository.findByLibraryIdAndFileHashAndSizeIncludeDeleted(
        existingSeries.libraryId,
        bookToCheck.fileHash,
        bookToCheck.fileSize
      )

      if (existingBooks.size > 1) {
        logger.warn { "Multiple matches: ${existingBooks}, limiting matching only withing series" }
        existingBooks = bookRepository.findBySeriesIdAndFileHashAndSizeIncludeDeleted(
          existingSeries.id,
          bookToCheck.fileHash,
          bookToCheck.fileSize
        )
      }
    }
    return existingBooks
  }

  private fun isModified(first: LocalDateTime, other: LocalDateTime): Boolean {
    return first.truncatedTo(ChronoUnit.MILLIS) != other.truncatedTo(ChronoUnit.MILLIS)
  }
}
