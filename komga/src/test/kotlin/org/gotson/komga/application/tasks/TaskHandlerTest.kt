package org.gotson.komga.application.tasks

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.jms.QUEUE_TASKS
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.config.JmsListenerEndpointRegistry
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.destination.JmsDestinationAccessor
import org.springframework.test.context.junit.jupiter.SpringExtension

private val logger = KotlinLogging.logger {}

@ExtendWith(SpringExtension::class)
@SpringBootTest
class TaskHandlerTest(
  @Autowired private val taskReceiver: TaskReceiver,
  @Autowired private val jmsTemplate: JmsTemplate,
  @Autowired private val jmsListenerEndpointRegistry: JmsListenerEndpointRegistry,
) {

  @MockkBean
  private lateinit var mockBookLifecycle: BookLifecycle

  @MockkBean
  private lateinit var mockBookRepository: BookRepository

  init {
    jmsTemplate.receiveTimeout = JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT
  }

  @AfterEach
  fun emptyQueue() {
    while (jmsTemplate.receive(QUEUE_TASKS) != null) {
      logger.info { "Emptying queue" }
    }
  }

  @Test
  fun `when similar tasks are submitted then only a few are executed`() {
    every { mockBookRepository.findByIdOrNull(any()) } returns makeBook("id")
    every { mockBookLifecycle.analyzeAndPersist(any()) } returns false

    jmsListenerEndpointRegistry.stop()

    repeat(100) {
      taskReceiver.analyzeBook("id")
    }

    jmsListenerEndpointRegistry.start()

    Thread.sleep(1_00)

    verify(exactly = 1) { mockBookLifecycle.analyzeAndPersist(any()) }
  }

  @Test
  fun `when high priority tasks are submitted then they are executed first`() {
    val slot = slot<String>()
    val calls = mutableListOf<Book>()
    every { mockBookRepository.findByIdOrNull(capture(slot)) } answers {
      Thread.sleep(1_00)
      makeBook(slot.captured)
    }
    every { mockBookLifecycle.analyzeAndPersist(capture(calls)) } returns false

    taskReceiver.analyzeBook("1", HIGHEST_PRIORITY)
    taskReceiver.analyzeBook("2", LOWEST_PRIORITY)
    taskReceiver.analyzeBook("3", HIGH_PRIORITY)
    taskReceiver.analyzeBook("4", HIGHEST_PRIORITY)
    taskReceiver.analyzeBook("5", DEFAULT_PRIORITY)

    Thread.sleep(1_000)

    verify(exactly = 5) { mockBookLifecycle.analyzeAndPersist(any()) }
    assertThat(calls.map { it.name }).containsExactly("1", "4", "3", "5", "2")
  }
}
