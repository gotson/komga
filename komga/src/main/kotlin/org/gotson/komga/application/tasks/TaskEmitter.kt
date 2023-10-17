package org.gotson.komga.application.tasks

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.service.BookConverter
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class TaskEmitter(
  private val bookRepository: BookRepository,
  private val bookConverter: BookConverter,
  private val tasksRepository: TasksRepository,
  private val eventPublisher: ApplicationEventPublisher,
) {
  fun scanLibrary(libraryId: String, scanDeep: Boolean = false, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.ScanLibrary(libraryId, scanDeep, priority))
  }

  fun emptyTrash(libraryId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.EmptyTrash(libraryId, priority))
  }

  fun analyzeUnknownAndOutdatedBooks(library: Library) {
    bookRepository.findAll(
      BookSearch(
        libraryIds = listOf(library.id),
        mediaStatus = listOf(Media.Status.UNKNOWN, Media.Status.OUTDATED),
      ),
      UnpagedSorted(Sort.by(Sort.Order.asc("seriesId"), Sort.Order.asc("number"))),
    ).forEach {
      submitTask(Task.AnalyzeBook(it.id, groupId = it.seriesId))
    }
  }

  fun hashBooksWithoutHash(library: Library) {
    if (library.hashFiles)
      bookRepository.findAllByLibraryIdAndWithEmptyHash(library.id).forEach {
        submitTask(Task.HashBook(it.id, LOWEST_PRIORITY))
      }
  }

  fun findBooksWithMissingPageHash(library: Library, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.FindBooksWithMissingPageHash(library.id, priority))
  }

  fun hashBookPages(bookIdToSeriesId: Collection<String>, priority: Int = DEFAULT_PRIORITY) {
    bookIdToSeriesId.forEach { submitTask(Task.HashBookPages(it, priority)) }
  }

  fun findBooksToConvert(library: Library, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.FindBooksToConvert(library.id, priority))
  }

  fun convertBookToCbz(books: Collection<Book>, priority: Int = DEFAULT_PRIORITY) {
    books.forEach { book ->
      submitTask(Task.ConvertBook(book.id, priority, book.seriesId))
    }
  }

  fun repairExtensions(library: Library, priority: Int = DEFAULT_PRIORITY) {
    if (library.repairExtensions)
      bookConverter.getMismatchedExtensionBooks(library).forEach {
        submitTask(Task.RepairExtension(it.id, priority, it.seriesId))
      }
  }

  fun findDuplicatePagesToDelete(library: Library, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.FindDuplicatePagesToDelete(library.id, priority))
  }

  fun removeDuplicatePages(bookId: String, pages: Collection<BookPageNumbered>, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RemoveHashedPages(bookId, pages, priority))
  }

  fun removeDuplicatePages(bookIdToPages: Map<String, Collection<BookPageNumbered>>, priority: Int = DEFAULT_PRIORITY) {
    bookIdToPages.forEach { removeDuplicatePages(it.key, it.value) }
  }

  fun analyzeBook(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.AnalyzeBook(book.id, priority, book.seriesId))
  }

  fun analyzeBook(books: Collection<Book>, priority: Int = DEFAULT_PRIORITY) {
    books.forEach { analyzeBook(it, priority) }
  }

  fun generateBookThumbnail(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.GenerateBookThumbnail(book.id, priority))
  }

  fun generateBookThumbnail(bookId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.GenerateBookThumbnail(bookId, priority))
  }

  fun generateBookThumbnail(bookIds: Collection<String>, priority: Int = DEFAULT_PRIORITY) {
    bookIds.forEach { generateBookThumbnail(it, priority) }
  }

  fun refreshBookMetadata(
    book: Book,
    capabilities: Set<BookMetadataPatchCapability> = BookMetadataPatchCapability.values().toSet(),
    priority: Int = DEFAULT_PRIORITY,
  ) {
    submitTask(Task.RefreshBookMetadata(book.id, capabilities, priority, book.seriesId))
  }

  fun refreshBookMetadata(
    books: Collection<Book>,
    capabilities: Set<BookMetadataPatchCapability> = BookMetadataPatchCapability.values().toSet(),
    priority: Int = DEFAULT_PRIORITY,
  ) {
    books.forEach { refreshBookMetadata(it, capabilities, priority) }
  }

  fun refreshSeriesMetadata(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshSeriesMetadata(seriesId, priority))
  }

  fun aggregateSeriesMetadata(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.AggregateSeriesMetadata(seriesId, priority))
  }

  fun refreshBookLocalArtwork(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshBookLocalArtwork(book.id, priority))
  }

  fun refreshBookLocalArtwork(books: Collection<Book>, priority: Int = DEFAULT_PRIORITY) {
    books.forEach { refreshBookLocalArtwork(it, priority) }
  }

  fun refreshSeriesLocalArtwork(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshSeriesLocalArtwork(seriesId, priority))
  }

  fun refreshSeriesLocalArtwork(seriesIds: Collection<String>, priority: Int = DEFAULT_PRIORITY) {
    seriesIds.forEach { refreshSeriesLocalArtwork(it, priority) }
  }

  fun importBook(sourceFile: String, seriesId: String, copyMode: CopyMode, destinationName: String?, upgradeBookId: String?, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.ImportBook(sourceFile, seriesId, copyMode, destinationName, upgradeBookId, priority))
  }

  fun rebuildIndex(priority: Int = DEFAULT_PRIORITY, entities: Set<LuceneEntity>? = null) {
    submitTask(Task.RebuildIndex(entities, priority))
  }

  fun upgradeIndex(priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.UpgradeIndex(priority))
  }

  fun deleteBook(bookId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.DeleteBook(bookId, priority))
  }

  fun deleteSeries(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.DeleteSeries(seriesId, priority))
  }

  fun fixThumbnailsWithoutMetadata(priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.FixThumbnailsWithoutMetadata(priority))
  }

  fun findBookThumbnailsToRegenerate(forBiggerResultOnly: Boolean, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.FindBookThumbnailsToRegenerate(forBiggerResultOnly, priority))
  }

  private fun submitTask(task: Task) {
    logger.info { "Sending task: $task" }
    tasksRepository.save(task)
    eventPublisher.publishEvent(TaskAddedEvent())
  }
}
