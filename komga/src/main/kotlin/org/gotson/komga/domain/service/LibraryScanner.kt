package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.hash.FileHasher
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
  private val mediaRepository: MediaRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val fileHasher: FileHasher,
  private val taskReceiver: TaskReceiver,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository
) {

  fun scanRootFolder(library: Library) {
    logger.info { "Updating library: $library" }
    measureTime {
      val scannedSeries =
        fileSystemScanner.scanRootFolder(Paths.get(library.root.toURI()), library.scanForceModifiedTime)
          .map { (series, books) ->
            series.copy(libraryId = library.id) to books.map { it.copy(libraryId = library.id) }
          }.map { (series, books) ->
            series to books.map { it.copy(fileHash = getBookHash(it)) }
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
        .let {
          bookRepository.softDeleteByBookIds(bookRepository.findAllIdBySeriesIds(it))
          seriesRepository.softDeleteAll(it)
        }
    } else {
      scannedSeries.keys.map { it.url }.let { urls ->
        val series = seriesRepository.findByLibraryIdAndUrlNotIn(library.id, urls).filterNot { it.deleted }
        if (series.isNotEmpty()) {
          logger.info { "Series is not on disk anymore, marking as deleted: $series" }
          series.map { it.id }.let {
            bookRepository.softDeleteByBookIds(bookRepository.findAllIdBySeriesIds(it))
            seriesRepository.softDeleteAll(it)
          }
        }
      }
    }
  }

  private fun updateSeriesAndBooks(scannedSeries: Map<Series, Collection<Book>>, library: Library) {
    scannedSeries.forEach { (newSeries, newBooks) ->
      seriesRepository.findByLibraryIdAndUrlIncludeDeleted(library.id, newSeries.url)?.let { existingSeries ->
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
      } ?: run {
        // if series does not exist, save it
        logger.info { "Adding new series: $newSeries" }
        seriesLifecycle.createSeries(newSeries)
      }
    }
  }

  private fun restoreDeletedBooksAndAddNew(scannedSeries: Map<Series, Collection<Book>>, library: Library) {
    scannedSeries.forEach { (newSeries, newBooks) ->
      seriesRepository.findByLibraryIdAndUrlIncludeDeleted(library.id, newSeries.url)?.let { existingSeries ->
        val existingBooks = bookRepository.findBySeriesId(existingSeries.id)
        val restoredBooks = restoreDeletedBooks(existingSeries, existingBooks, newBooks)

        val booksToAdd = newBooks
          .filterNot { newBook -> existingBooks.map { it.url }.contains(newBook.url) }
          .filterNot { toCheck -> restoredBooks.map { it.fileHash }.contains(toCheck.fileHash) }

        if (booksToAdd.isNotEmpty()) {
          logger.info { "Adding new books: $booksToAdd" }
          seriesLifecycle.addBooks(existingSeries, booksToAdd)
          seriesLifecycle.sortBooks(existingSeries)
        }
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
          logger.debug { "matched multiple series: ${matchedSeries.map { it.path() }}, limiting matching to the library" }
          matchedSeries = matchedSeries.filter { it.libraryId == newSeries.libraryId }
          if (matchedSeries.size > 1) {
            logger.debug { "matched multiple series with similar books: ${matchedSeries.map { it.path() }}. Series will be added as a new one" }
          }
        }

        if (matchedSeries.size == 1) {
          val seriesToRestore = matchedSeries.first()
          logger.info { "new series contains all books from previously deleted series. Restoring old series: ${seriesToRestore.path()}" }

          val deletedBooks: Collection<Book> = bookRepository.findBySeriesIdIncludeDeleted(seriesToRestore.id)

          val restoredBooks: List<Book> = newBooks.mapNotNull { newBook ->
            deletedBooks.find { Pair(it.fileHash, it.fileSize) == Pair(newBook.fileHash, newBook.fileSize) }?.let {
              newBook.copy(id = it.id, seriesId = it.seriesId)
            }
          }
          val restoredSeries = newSeries.copy(id = seriesToRestore.id, createdDate = seriesToRestore.createdDate)

          bookRepository.updateMany(restoredBooks)
          seriesRepository.update(restoredSeries)
          updateBookMetadataTitle(restoredBooks)
          updateSeriesMetadataTitle(restoredSeries)
        }
      }
  }

  private fun restoreDeletedBooks(existingSeries: Series, existingBooks: Collection<Book>, booksToCheck: Collection<Book>): Collection<Book> {
    val booksToRestore = booksToCheck.asSequence()
      .filterNot { newBook -> existingBooks.map { it.fileHash }.contains(newBook.fileHash) }
      .map { it to matchExistingBooksByHash(it, existingSeries) }
      .filter {
        if (it.second.size > 1) logger.debug { "Multiple matches: ${it.second}, skipping entry" }
        it.second.size == 1
      }
      .map { it.first to it.second.first() }
      .map { it.first.copy(id = it.second.id, seriesId = existingSeries.id, deleted = false) }.toList()

    if (booksToRestore.isNotEmpty()) {
      logger.info { "Restoring deleted books: $booksToRestore" }
      bookRepository.updateMany(booksToRestore)
      updateBookMetadataTitle(booksToRestore)
    }

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
        bookRepository.softDeleteByBookIds(books.map { it.id })
      }
  }

  private fun matchExistingBooksByHash(bookToCheck: Book, existingSeries: Series): Collection<Book> {
    var existingBooks: Collection<Book> =
      bookRepository.findByFileHashAndSizeIncludeDeleted(bookToCheck.fileHash, bookToCheck.fileSize)
        //check if book exists in case it was deleted but library analysis wasn't done yet
        .filter { it.deleted || Files.notExists(it.path()) }
    if (existingBooks.size > 1) {
      logger.debug { "Multiple matches: ${existingBooks}, limiting matching to library of the series" }
      existingBooks = existingBooks.filter { it.libraryId == existingSeries.libraryId }

      if (existingBooks.size > 1) {
        logger.debug { "Multiple matches: ${existingBooks}, limiting matching only withing series" }
        existingBooks = existingBooks.filter { it.seriesId == existingSeries.id }
      }
    }
    return existingBooks
  }

  private fun isModified(first: LocalDateTime, other: LocalDateTime): Boolean {
    return first.truncatedTo(ChronoUnit.MILLIS) != other.truncatedTo(ChronoUnit.MILLIS)
  }

  private fun updateBookMetadataTitle(books: Collection<Book>) {
    books.forEach { book ->
      val meta = bookMetadataRepository.findById(book.id)
      if (!meta.titleLock) {
        bookMetadataRepository.update(meta.copy(title = book.name))
        taskReceiver.refreshBookMetadata(book)
      }
    }
  }

  private fun updateSeriesMetadataTitle(series: Series) {
    val meta = seriesMetadataRepository.findById(series.id)
    if (!meta.titleLock) {
      seriesMetadataRepository.update(meta.copy(title = series.name))
      taskReceiver.refreshSeriesMetadata(series.id)
    }
  }
}
