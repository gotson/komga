package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.SidecarRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.infrastructure.hash.Hasher
import org.gotson.komga.language.notEquals
import org.gotson.komga.language.toIndexedMap
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

@Service
class LibraryContentLifecycle(
  private val fileSystemScanner: FileSystemScanner,
  private val seriesRepository: SeriesRepository,
  private val bookRepository: BookRepository,
  private val libraryRepository: LibraryRepository,
  private val bookLifecycle: BookLifecycle,
  private val mediaRepository: MediaRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val collectionLifecycle: SeriesCollectionLifecycle,
  private val readListLifecycle: ReadListLifecycle,
  private val sidecarRepository: SidecarRepository,
  private val komgaSettingsProvider: KomgaSettingsProvider,
  private val taskEmitter: TaskEmitter,
  private val transactionTemplate: TransactionTemplate,
  private val hasher: Hasher,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val readListRepository: ReadListRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val eventPublisher: ApplicationEventPublisher,
  private val thumbnailSeriesRepository: ThumbnailSeriesRepository,
) {
  fun scanRootFolder(
    library: Library,
    scanDeep: Boolean = false,
  ) {
    logger.info { "Scan root folder for library: $library" }
    measureTime {
      val scanResult =
        try {
          fileSystemScanner.scanRootFolder(
            Paths.get(library.root.toURI()),
            library.scanForceModifiedTime,
            library.oneshotsDirectory,
            library.scanCbx,
            library.scanPdf,
            library.scanEpub,
            library.scanDirectoryExclusions,
          )
        } catch (e: DirectoryNotFoundException) {
          library.copy(unavailableDate = LocalDateTime.now()).let {
            libraryRepository.update(it)
            eventPublisher.publishEvent(DomainEvent.LibraryUpdated(it))
          }
          throw e
        }

      if (library.unavailableDate != null) {
        library.copy(unavailableDate = null).let {
          libraryRepository.update(it)
          eventPublisher.publishEvent(DomainEvent.LibraryUpdated(it))
        }
      }

      val scannedSeries =
        scanResult
          .series
          .map { (series, books) ->
            series.copy(libraryId = library.id) to books.map { it.copy(libraryId = library.id) }
          }.toMap()

      // delete series that don't exist anymore
      if (scannedSeries.isEmpty()) {
        logger.info { "Scan returned no series, soft deleting all existing series" }
        val series = seriesRepository.findAllByLibraryId(library.id)
        seriesLifecycle.softDeleteMany(series)
      } else {
        scannedSeries.keys.map { it.url }.let { urls ->
          val series = seriesRepository.findAllNotDeletedByLibraryIdAndUrlNotIn(library.id, urls)
          if (series.isNotEmpty()) {
            logger.info { "Soft deleting series not on disk anymore: $series" }
            seriesLifecycle.softDeleteMany(series)
          }
        }
      }

      // delete books that don't exist anymore. We need to do this now, so trash bin can work
      val seriesToSortAndRefresh =
        scannedSeries.values.flatten().map { it.url }.let { urls ->
          val books = bookRepository.findAllNotDeletedByLibraryIdAndUrlNotIn(library.id, urls)
          if (books.isNotEmpty()) {
            logger.info { "Soft deleting books not on disk anymore: $books" }
            bookLifecycle.softDeleteMany(books)
            books
              .map { it.seriesId }
              .distinct()
              .mapNotNull { seriesRepository.findByIdOrNull(it) }
              .toMutableList()
          } else {
            mutableListOf()
          }
        }
      // we store the url of all the series that had deleted books
      // this can be used to detect changed series even if their file modified date did not change, for example because of NFS/SMB cache
      val seriesUrlWithDeletedBooks = seriesToSortAndRefresh.map { it.url }

      scannedSeries.forEach { (newSeries, newBooks) ->
        val existingSeries = seriesRepository.findNotDeletedByLibraryIdAndUrlOrNull(library.id, newSeries.url)

        // if series does not exist, save it
        if (existingSeries == null) {
          logger.info { "Adding new series: $newSeries" }
          val createdSeries = seriesLifecycle.createSeries(newSeries)
          seriesLifecycle.addBooks(createdSeries, newBooks)
          tryRestoreSeries(createdSeries, newBooks)
          tryRestoreBooks(newBooks)
          seriesToSortAndRefresh.add(createdSeries)
        } else {
          // if series already exists, update it
          logger.debug { "Scanned series already exists. Scanned: $newSeries, Existing: $existingSeries" }
          val seriesChanged = newSeries.fileLastModified.notEquals(existingSeries.fileLastModified) || existingSeries.deletedDate != null || seriesUrlWithDeletedBooks.contains(newSeries.url)
          if (seriesChanged) {
            logger.info { "Series changed on disk, updating: $existingSeries" }
            seriesRepository.update(existingSeries.copy(fileLastModified = newSeries.fileLastModified, deletedDate = null))
          }
          if (scanDeep || seriesChanged) {
            // update list of books with existing entities if they exist
            val existingBooks = bookRepository.findAllBySeriesId(existingSeries.id)
            logger.debug { "Existing books: $existingBooks" }

            // update existing books
            newBooks.forEach { newBook ->
              logger.debug { "Trying to match scanned book by url: $newBook" }
              existingBooks.find { it.url == newBook.url && it.deletedDate == null }?.let { existingBook ->
                logger.debug { "Matched existing book: $existingBook" }
                if (newBook.fileLastModified.notEquals(existingBook.fileLastModified)) {
                  val hash =
                    if (existingBook.fileSize == newBook.fileSize && existingBook.fileHash.isNotBlank()) {
                      hasher.computeHash(newBook.path)
                    } else {
                      null
                    }
                  if (hash == existingBook.fileHash) {
                    logger.info { "Book changed on disk, but still has the same hash, no need to reset media status: $existingBook" }
                    val updatedBook =
                      existingBook.copy(
                        fileLastModified = newBook.fileLastModified,
                        fileSize = newBook.fileSize,
                        fileHash = hash,
                      )
                    bookRepository.update(updatedBook)
                  } else {
                    logger.info { "Book changed on disk, update and reset media status: $existingBook" }
                    val updatedBook =
                      existingBook.copy(
                        fileLastModified = newBook.fileLastModified,
                        fileSize = newBook.fileSize,
                        fileHash = hash ?: "",
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
            }

            // add new books
            val existingBooksUrls = existingBooks.filterNot { it.deletedDate != null }.map { it.url }
            val booksToAdd = newBooks.filterNot { newBook -> existingBooksUrls.contains(newBook.url) }
            logger.info { "Adding new books: $booksToAdd" }
            seriesLifecycle.addBooks(existingSeries, booksToAdd)
            tryRestoreBooks(booksToAdd)
            seriesToSortAndRefresh.add(existingSeries)
          }
        }
      }

      // for all series where books have been removed or added, trigger a sort and refresh metadata
      seriesToSortAndRefresh.distinctBy { it.id }.forEach {
        seriesLifecycle.sortBooks(it)
        taskEmitter.refreshSeriesMetadata(it.id)
      }

      val existingSidecars = sidecarRepository.findAll()
      scanResult.sidecars.forEach { newSidecar ->
        val existingSidecar = existingSidecars.firstOrNull { it.url == newSidecar.url }
        if (existingSidecar == null || existingSidecar.lastModifiedTime.notEquals(newSidecar.lastModifiedTime)) {
          when (newSidecar.source) {
            Sidecar.Source.SERIES ->
              seriesRepository.findNotDeletedByLibraryIdAndUrlOrNull(library.id, newSidecar.parentUrl)?.let { series ->
                logger.info { "Sidecar changed on disk (${newSidecar.url}, refresh Series for ${newSidecar.type}: $series" }
                when (newSidecar.type) {
                  Sidecar.Type.ARTWORK -> taskEmitter.refreshSeriesLocalArtwork(series.id)
                  Sidecar.Type.METADATA -> taskEmitter.refreshSeriesMetadata(series.id)
                }
              }

            Sidecar.Source.BOOK ->
              bookRepository.findNotDeletedByLibraryIdAndUrlOrNull(library.id, newSidecar.parentUrl)?.let { book ->
                logger.info { "Sidecar changed on disk (${newSidecar.url}, refresh Book for ${newSidecar.type}: $book" }
                when (newSidecar.type) {
                  Sidecar.Type.ARTWORK -> taskEmitter.refreshBookLocalArtwork(book)
                  Sidecar.Type.METADATA -> taskEmitter.refreshBookMetadata(book)
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

      if (library.emptyTrashAfterScan)
        emptyTrash(library)
      else
        cleanupEmptySets()
    }.also { logger.info { "Library updated in $it" } }

    eventPublisher.publishEvent(DomainEvent.LibraryScanned(library))
  }

  /**
   * This will try to match newSeries with a deleted series.
   * Series are matched if:
   * - they have the same number of books
   * - all the books are matched by file size and file hash
   *
   * If a series is matched, the following will be restored from the deleted series to the new series:
   * - Collections
   * - Metadata. The metadata title will only be copied if locked. If not locked, the folder name is used.
   * - all books, via #tryRestoreBooks
   */
  private fun tryRestoreSeries(
    newSeries: Series,
    newBooks: List<Book>,
  ) {
    logger.info { "Try to restore series: $newSeries" }
    val bookSizes = newBooks.map { it.fileSize }

    val deletedCandidates =
      seriesRepository
        .findAll(SearchCondition.Deleted(SearchOperator.IsTrue), SearchContext.empty(), Pageable.unpaged())
        .content
        .mapNotNull { deletedCandidate ->
          val deletedBooks = bookRepository.findAllBySeriesId(deletedCandidate.id)
          val deletedBooksSizes = deletedBooks.map { it.fileSize }
          if (newBooks.size == deletedBooks.size && bookSizes.containsAll(deletedBooksSizes) && deletedBooksSizes.containsAll(bookSizes) && deletedBooks.all { it.fileHash.isNotBlank() }) {
            deletedCandidate to deletedBooks
          } else {
            null
          }
        }
    logger.debug { "Deleted series candidates: $deletedCandidates" }

    if (deletedCandidates.isNotEmpty()) {
      val newBooksWithHash = newBooks.map { book -> bookRepository.findByIdOrNull(book.id)!!.copy(fileHash = hasher.computeHash(book.path)) }
      bookRepository.update(newBooksWithHash)

      val match =
        deletedCandidates.find { (_, books) ->
          books.map { it.fileHash }.containsAll(newBooksWithHash.map { it.fileHash }) && newBooksWithHash.map { it.fileHash }.containsAll(books.map { it.fileHash })
        }

      if (match != null) {
        // restore series
        logger.info { "Match found, restore $match into $newSeries" }
        transactionTemplate.executeWithoutResult {
          // copy metadata
          seriesMetadataRepository.findById(match.first.id).let { deleted ->
            val newlyAdded = seriesMetadataRepository.findById(newSeries.id)
            seriesMetadataRepository.update(
              deleted.copy(
                seriesId = newSeries.id,
                title = if (deleted.titleLock) deleted.title else newlyAdded.title,
                titleSort = if (deleted.titleSortLock) deleted.titleSort else newlyAdded.titleSort,
              ),
            )
          }

          // copy user uploaded thumbnails
          thumbnailSeriesRepository.findAllBySeriesIdIdAndType(match.first.id, ThumbnailSeries.Type.USER_UPLOADED).forEach { deleted ->
            thumbnailSeriesRepository.update(deleted.copy(seriesId = newSeries.id))
          }

          // replace deleted series by new series in collections
          collectionRepository
            .findAllContainingSeriesId(match.first.id, filterOnLibraryIds = null)
            .forEach { col ->
              collectionRepository.update(
                col.copy(
                  seriesIds = col.seriesIds.map { if (it == match.first.id) newSeries.id else it },
                ),
              )
            }

          tryRestoreBooks(newBooksWithHash)

          // delete upgraded series
          seriesLifecycle.deleteMany(listOf(match.first))
        }
      }
    }
  }

  /**
   * This will try to match each book in newBooks with a deleted book.
   * Books are matched by file size, then by file hash.
   *
   * If a book is matched, the following will be restored from the deleted book to the new book:
   * - Media
   * - Read Progress
   * - Read Lists
   * - Metadata. The metadata title will only be copied if locked. If not locked, the filename is used, but a refresh for Title will be requested.
   */
  private fun tryRestoreBooks(newBooks: List<Book>) {
    logger.info { "Try to restore books: $newBooks" }
    newBooks.forEach { bookToAdd ->
      // try to find a deleted book that matches the file size
      val deletedCandidates = bookRepository.findAllDeletedByFileSize(bookToAdd.fileSize).filter { it.fileHash.isNotBlank() }
      logger.debug { "Deleted candidates: $deletedCandidates" }

      if (deletedCandidates.isNotEmpty()) {
        // if the book has no hash, compute the hash and store it
        val bookWithHash =
          if (bookToAdd.fileHash.isNotBlank())
            bookToAdd
          else
            bookRepository.findByIdOrNull(bookToAdd.id)!!.copy(fileHash = hasher.computeHash(bookToAdd.path)).also { bookRepository.update(it) }

        val match = deletedCandidates.find { it.fileHash == bookWithHash.fileHash }

        if (match != null) {
          // restore book
          logger.info { "Match found, restore $match into $bookToAdd" }
          transactionTemplate.executeWithoutResult {
            // copy media
            mediaRepository.findById(match.id).let { deleted ->
              mediaRepository.update(deleted.copy(bookId = bookToAdd.id))
            }

            // copy generated and user uploaded thumbnails
            thumbnailBookRepository.findAllByBookIdAndType(match.id, setOf(ThumbnailBook.Type.GENERATED, ThumbnailBook.Type.USER_UPLOADED)).forEach { deleted ->
              thumbnailBookRepository.update(deleted.copy(bookId = bookToAdd.id))
            }

            // copy metadata
            bookMetadataRepository.findById(match.id).let { deleted ->
              val newlyAdded = bookMetadataRepository.findById(bookToAdd.id)
              bookMetadataRepository.update(
                deleted.copy(
                  bookId = bookToAdd.id,
                  title = if (deleted.titleLock) deleted.title else newlyAdded.title,
                ),
              )
              if (!deleted.titleLock) taskEmitter.refreshBookMetadata(bookToAdd, setOf(BookMetadataPatchCapability.TITLE))
            }

            // copy read progress
            readProgressRepository
              .findAllByBookId(match.id)
              .map { it.copy(bookId = bookToAdd.id) }
              .forEach { readProgressRepository.save(it) }

            // replace deleted book by new book in read lists
            readListRepository
              .findAllContainingBookId(match.id, filterOnLibraryIds = null)
              .forEach { rl ->
                readListRepository.update(
                  rl.copy(
                    bookIds =
                      rl.bookIds.values
                        .map { if (it == match.id) bookToAdd.id else it }
                        .toIndexedMap(),
                  ),
                )
              }

            // delete soft-deleted book
            bookLifecycle.deleteOne(match)
          }
        }
      }
    }
  }

  fun emptyTrash(library: Library) {
    logger.info { "Empty trash for library: $library" }

    val seriesToDelete =
      seriesRepository
        .findAll(
          SearchCondition.AllOfSeries(
            SearchCondition.LibraryId(SearchOperator.Is(library.id)),
            SearchCondition.Deleted(SearchOperator.IsTrue),
          ),
          SearchContext.empty(),
          Pageable.unpaged(),
        ).content
    seriesLifecycle.deleteMany(seriesToDelete)

    val booksToDelete =
      bookRepository
        .findAll(
          SearchCondition.AllOfBook(
            SearchCondition.LibraryId(SearchOperator.Is(library.id)),
            SearchCondition.Deleted(SearchOperator.IsTrue),
          ),
          SearchContext.empty(),
          Pageable.unpaged(),
        ).content
    bookLifecycle.deleteMany(booksToDelete)
    booksToDelete.map { it.seriesId }.distinct().forEach { seriesId ->
      seriesRepository.findByIdOrNull(seriesId)?.let { seriesLifecycle.sortBooks(it) }
    }

    cleanupEmptySets()
  }

  private fun cleanupEmptySets() {
    if (komgaSettingsProvider.deleteEmptyCollections) {
      collectionLifecycle.deleteEmptyCollections()
    }

    if (komgaSettingsProvider.deleteEmptyReadLists) {
      readListLifecycle.deleteEmptyReadLists()
    }
  }
}
