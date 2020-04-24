package org.gotson.komga.application.tasks

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import mu.KotlinLogging
import org.gotson.komga.application.service.MetadataLifecycle
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.destination.JmsDestinationAccessor
import org.springframework.test.context.junit.jupiter.SpringExtension

private val logger = KotlinLogging.logger {}

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureTestDatabase
class TaskHandlerTest(
  @Autowired private val taskReceiver: TaskReceiver,
  @Autowired private val jmsTemplate: JmsTemplate,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val seriesRepository: SeriesRepository
) {

  @MockkBean
  private lateinit var mockMetadataLifecycle: MetadataLifecycle

  private val library = makeLibrary()

  init {
    jmsTemplate.receiveTimeout = JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT
  }

  @BeforeAll
  fun `setup library`() {
    libraryRepository.save(library)
  }

  @AfterAll
  fun `teardown library`() {
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun `clear repository`() {
    seriesRepository.deleteAll()
  }

  @AfterEach
  fun emptyQueue() {
    while (jmsTemplate.receive(QUEUE_TASKS) != null) {
      logger.info { "Emptying queue" }
    }
  }

  @Test
  fun `when similar tasks are submitted then only a few are executed`() {
    val book = makeBook("book")
    val series = makeSeries("series", listOf(book)).also { it.library = library }
    seriesRepository.save(series)

    every { mockMetadataLifecycle.refreshMetadata(any()) } answers { Thread.sleep(1_000) }

    repeat(100) {
      taskReceiver.refreshBookMetadata(book)
    }

    Thread.sleep(5_000)

    verify(atLeast = 1, atMost = 3) { mockMetadataLifecycle.refreshMetadata(any()) }
  }
}
