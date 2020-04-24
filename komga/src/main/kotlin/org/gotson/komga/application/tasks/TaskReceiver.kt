package org.gotson.komga.application.tasks

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_UNIQUE_ID
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class TaskReceiver(
  private val jmsTemplate: JmsTemplate,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository
) {

  fun scanLibraries() {
    libraryRepository.findAll().forEach { scanLibrary(it) }
  }

  fun scanLibrary(library: Library) {
    submitTask(Task.ScanLibrary(library.id))
  }

  fun analyzeUnknownBooks(library: Library) {
    bookRepository.findAllByMediaStatusAndSeriesLibrary(Media.Status.UNKNOWN, library).forEach {
      submitTask(Task.AnalyzeBook(it.id))
    }
  }

  fun analyzeBook(book: Book) {
    submitTask(Task.AnalyzeBook(book.id))
  }

  fun generateBookThumbnail(book: Book) {
    submitTask(Task.GenerateBookThumbnail(book.id))
  }

  fun refreshBookMetadata(book: Book) {
    submitTask(Task.RefreshBookMetadata(book.id))
  }

  private fun submitTask(task: Task) {
    logger.info { "Sending task: $task" }
    jmsTemplate.convertAndSend(QUEUE_TASKS, task) {
      it.apply {
        setStringProperty(QUEUE_TYPE, QUEUE_TASKS_TYPE)
        setStringProperty(QUEUE_UNIQUE_ID, task.uniqueId())
      }
    }
  }
}
