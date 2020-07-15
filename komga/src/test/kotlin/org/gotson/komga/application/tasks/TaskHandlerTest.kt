package org.gotson.komga.application.tasks

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.domain.service.MetadataLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.destination.JmsDestinationAccessor
import org.springframework.test.context.junit.jupiter.SpringExtension

private val logger = KotlinLogging.logger {}

@ExtendWith(SpringExtension::class)
@SpringBootTest
class TaskHandlerTest(
  @Autowired private val taskReceiver: TaskReceiver,
  @Autowired private val jmsTemplate: JmsTemplate,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesLifecycle: SeriesLifecycle,
  @Autowired private val libraryLifecycle: LibraryLifecycle
) {

  @MockkBean
  private lateinit var mockMetadataLifecycle: MetadataLifecycle

  private val library = makeLibrary()

  init {
    jmsTemplate.receiveTimeout = JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT
  }

  @BeforeAll
  fun `setup library`() {
    libraryRepository.insert(library)
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    libraryLifecycle.deleteLibrary(library)
  }

  @AfterEach
  fun emptyQueue() {
    while (jmsTemplate.receive(QUEUE_TASKS) != null) {
      logger.info { "Emptying queue" }
    }
  }

  @Test
  fun `when similar tasks are submitted then only a few are executed`() {
    val book = makeBook("book", libraryId = library.id)
    val series = makeSeries("series", libraryId = library.id)
    seriesLifecycle.createSeries(series).let {
      seriesLifecycle.addBooks(it, listOf(book))
    }

    every { mockMetadataLifecycle.refreshMetadata(any<Book>()) } answers { Thread.sleep(1_000) }
    every { mockMetadataLifecycle.refreshMetadata(any<Series>()) } just runs

    val createdBook = bookRepository.findAll().first()

    repeat(100) {
      taskReceiver.refreshBookMetadata(createdBook)
    }

    Thread.sleep(5_000)

    verify(atLeast = 1, atMost = 3) { mockMetadataLifecycle.refreshMetadata(any<Book>()) }
  }
}
