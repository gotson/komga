package org.gotson.komga.application.tasks

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.BookConverter
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_UNIQUE_ID
import org.springframework.data.domain.Sort
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class TaskReceiver(
  private val jmsTemplate: JmsTemplate,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val bookConverter: BookConverter,
) {

  fun scanLibraries() {
    libraryRepository.findAll().forEach { scanLibrary(it.id) }
  }

  fun scanLibrary(libraryId: String) {
    submitTask(Task.ScanLibrary(libraryId))
  }

  fun analyzeUnknownAndOutdatedBooks(library: Library) {
    bookRepository.findAllId(
      BookSearch(
        libraryIds = listOf(library.id),
        mediaStatus = listOf(Media.Status.UNKNOWN, Media.Status.OUTDATED)
      ),
      Sort.by(Sort.Order.asc("seriesId"), Sort.Order.asc("number"))
    ).forEach {
      submitTask(Task.AnalyzeBook(it))
    }
  }

  fun convertBooksToCbz(library: Library, priority: Int = DEFAULT_PRIORITY) {
    bookConverter.getConvertibleBookIds(library).forEach {
      submitTask(Task.ConvertBook(it, priority))
    }
  }

  fun repairExtensions(library: Library, priority: Int = DEFAULT_PRIORITY) {
    bookConverter.getMismatchedExtensionBookIds(library).forEach {
      submitTask(Task.RepairExtension(it, priority))
    }
  }

  fun analyzeBook(bookId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.AnalyzeBook(bookId, priority))
  }

  fun analyzeBook(book: Book, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.AnalyzeBook(book.id, priority))
  }

  fun generateBookThumbnail(bookId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.GenerateBookThumbnail(bookId, priority))
  }

  fun refreshBookMetadata(
    bookId: String,
    capabilities: List<BookMetadataPatchCapability> = BookMetadataPatchCapability.values().toList(),
    priority: Int = DEFAULT_PRIORITY,
  ) {
    submitTask(Task.RefreshBookMetadata(bookId, capabilities, priority))
  }

  fun refreshBookMetadata(
    book: Book,
    capabilities: List<BookMetadataPatchCapability> = BookMetadataPatchCapability.values().toList(),
    priority: Int = DEFAULT_PRIORITY,
  ) {
    submitTask(Task.RefreshBookMetadata(book.id, capabilities, priority))
  }

  fun refreshSeriesMetadata(seriesId: String) {
    submitTask(Task.RefreshSeriesMetadata(seriesId))
  }

  fun aggregateSeriesMetadata(seriesId: String) {
    submitTask(Task.AggregateSeriesMetadata(seriesId))
  }

  fun importBook(sourceFile: String, seriesId: String, copyMode: CopyMode, destinationName: String?, upgradeBookId: String?, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.ImportBook(sourceFile, seriesId, copyMode, destinationName, upgradeBookId, priority))
  }

  private fun submitTask(task: Task) {
    logger.info { "Sending task: $task" }
    jmsTemplate.priority = task.priority
    jmsTemplate.convertAndSend(QUEUE_TASKS, task) {
      it.apply {
        setStringProperty(QUEUE_TYPE, QUEUE_TASKS_TYPE)
        setStringProperty(QUEUE_UNIQUE_ID, task.uniqueId())
      }
    }
  }
}
