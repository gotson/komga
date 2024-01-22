package org.gotson.komga.application.tasks

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
class TaskProcessorTest(
  @Autowired private val taskEmitter: TaskEmitter,
  @Autowired private val taskProcessor: TaskProcessor,
) {
  @MockkBean
  private lateinit var mockBookLifecycle: BookLifecycle

  @MockkBean
  private lateinit var mockBookRepository: BookRepository

  fun testTasks(
    sleep: Duration = 3.seconds,
    block: () -> Unit,
  ) {
    taskProcessor.processTasks = false
    block()
    taskProcessor.processTasks = true
    taskProcessor.processAvailableTask()
    Thread.sleep(sleep.inWholeMilliseconds)
  }

  @Test
  fun `when similar tasks are submitted then only one is executed`() {
    every { mockBookRepository.findByIdOrNull(any()) } returns makeBook("id")
    every { mockBookLifecycle.analyzeAndPersist(any()) } returns emptySet()

    val book = makeBook("book")

    testTasks {
      repeat(100) {
        taskEmitter.analyzeBook(book)
      }
    }

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
    every { mockBookLifecycle.analyzeAndPersist(capture(calls)) } returns emptySet()

    testTasks {
      (0..9).forEach {
        taskEmitter.analyzeBook(makeBook("$it", id = "$it"), it)
      }
    }

    verify(exactly = 10) { mockBookLifecycle.analyzeAndPersist(any()) }
    assertThat(calls.map { it.name }).containsExactlyElementsOf((9 downTo 0).map { "$it" })
  }
}
