package org.gotson.komga.application.tasks

import mu.KotlinLogging
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.BookConverter
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.gotson.komga.infrastructure.jms.QUEUE_SUB_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_UNIQUE_ID
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.springframework.data.domain.Sort
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import javax.jms.ConnectionFactory

private val logger = KotlinLogging.logger {}

@Service
class TaskReceiver(
  connectionFactory: ConnectionFactory,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val bookConverter: BookConverter,
  private val komgaProperties: KomgaProperties,
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
    bookRepository.findAllIds(
      BookSearch(
        libraryIds = listOf(library.id),
        mediaStatus = listOf(Media.Status.UNKNOWN, Media.Status.OUTDATED),
      ),
      Sort.by(Sort.Order.asc("seriesId"), Sort.Order.asc("number")),
    ).forEach {
      submitTask(Task.AnalyzeBook(it))
    }
  }

  fun hashBooksWithoutHash(library: Library) {
    if (komgaProperties.fileHashing)
      bookRepository.findAllIdsByLibraryIdAndWithEmptyHash(library.id).forEach {
        submitTask(Task.HashBook(it, LOWEST_PRIORITY))
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

  fun generateBookThumbnail(bookId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.GenerateBookThumbnail(bookId, priority))
  }

  fun refreshBookMetadata(
    bookId: String,
    capabilities: Set<BookMetadataPatchCapability> = BookMetadataPatchCapability.values().toSet(),
    priority: Int = DEFAULT_PRIORITY,
  ) {
    submitTask(Task.RefreshBookMetadata(bookId, capabilities, priority))
  }

  fun refreshSeriesMetadata(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshSeriesMetadata(seriesId, priority))
  }

  fun aggregateSeriesMetadata(seriesId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.AggregateSeriesMetadata(seriesId, priority))
  }

  fun refreshBookLocalArtwork(bookId: String, priority: Int = DEFAULT_PRIORITY) {
    submitTask(Task.RefreshBookLocalArtwork(bookId, priority))
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
        setStringProperty(QUEUE_TYPE, QUEUE_TASKS_TYPE)
        setStringProperty(QUEUE_UNIQUE_ID, task.uniqueId())
        setStringProperty(QUEUE_SUB_TYPE, task::class.simpleName)
      }
    }
  }
}
