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
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.BookConverter
import org.gotson.komga.infrastructure.jms.JMS_PROPERTY_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS
import org.gotson.komga.infrastructure.jms.QUEUE_UNIQUE_ID
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.springframework.data.domain.Sort
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import javax.jms.ConnectionFactory

private val logger = KotlinLogging.logger {}

@Service
class TaskEmitter(
  connectionFactory: ConnectionFactory,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val bookConverter: BookConverter,
) {

  private val jmsTemplates = (0..9).associateWith {
    JmsTemplate(connectionFactory).apply {
      priority = it
      isExplicitQosEnabled = true
    }
  }

  fun scanLibraries() {
    libraryRepository.findAll().forEach { scanLibrary(it.id) }
  }

  fun scanLibrary(libraryId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.ScanLibrary(libraryId, priority))
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
        submitTask(Task.HashBook(it.id, LOWEST_PRIORITY, it.seriesId))
      }
  }

  fun findBooksWithMissingPageHash(library: Library, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.FindBooksWithMissingPageHash(library.id, priority))
  }

  fun hashBookPages(bookId: String, seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.HashBookPages(bookId, priority, seriesId))
  }

  fun findBooksToConvert(library: Library, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.FindBooksToConvert(library.id, priority))
  }

  fun convertBookToCbz(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.ConvertBook(book.id, priority, book.seriesId))
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
    submitTask(Task.RemoveHashedPages(bookId, pages, priority, bookId))
  }

  fun analyzeBook(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.AnalyzeBook(book.id, priority, book.seriesId))
  }

  fun generateBookThumbnail(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.GenerateBookThumbnail(book.id, priority, book.seriesId))
  }

  fun refreshBookMetadata(
    book: Book,
    capabilities: Set<BookMetadataPatchCapability> = BookMetadataPatchCapability.values().toSet(),
    priority: Int = DEFAULT_PRIORITY,
  ) {
    submitTask(Task.RefreshBookMetadata(book.id, capabilities, priority, book.seriesId))
  }

  fun refreshSeriesMetadata(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshSeriesMetadata(seriesId, priority))
  }

  fun aggregateSeriesMetadata(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.AggregateSeriesMetadata(seriesId, priority))
  }

  fun refreshBookLocalArtwork(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshBookLocalArtwork(book.id, priority, book.seriesId))
  }

  fun refreshSeriesLocalArtwork(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshSeriesLocalArtwork(seriesId, priority))
  }

  fun importBook(sourceFile: String, seriesId: String, copyMode: CopyMode, destinationName: String?, upgradeBookId: String?, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.ImportBook(sourceFile, seriesId, copyMode, destinationName, upgradeBookId, priority))
  }

  fun rebuildIndex(priority: Int = DEFAULT_PRIORITY, entities: Set<LuceneEntity>? = null) {
    submitTask(Task.RebuildIndex(entities, priority))
  }

  fun deleteBook(bookId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.DeleteBook(bookId, priority))
  }

  fun deleteSeries(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.DeleteSeries(seriesId, priority))
  }

  private fun submitTask(task: Task) {
    logger.info { "Sending task: $task" }
    jmsTemplates[task.priority]!!.convertAndSend(QUEUE_TASKS, task) {
      it.apply {
        setStringProperty(QUEUE_UNIQUE_ID, task.uniqueId())
        setStringProperty(JMS_PROPERTY_TYPE, task.javaClass.simpleName)
        task.groupId?.let { groupId -> setStringProperty("JMSXGroupID", groupId) }
      }
    }
  }
}
