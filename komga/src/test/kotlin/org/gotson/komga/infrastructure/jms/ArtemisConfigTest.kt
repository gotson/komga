package org.gotson.komga.infrastructure.jms

import com.ninjasquad.springmockk.MockkBean
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.application.tasks.TaskHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.destination.JmsDestinationAccessor
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.jms.QueueBrowser
import javax.jms.Session

private val logger = KotlinLogging.logger {}

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ArtemisConfigTest(
  @Autowired private val jmsTemplate: JmsTemplate,
) {

  @MockkBean
  private lateinit var taskHandler: TaskHandler // to avoid the taskHandler from picking up the messages

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
  fun `when sending messages with the same unique id then messages are deduplicated`() {
    for (i in 1..5) {
      jmsTemplate.convertAndSend(
        QUEUE_TASKS,
        "message $i",
      ) {
        it.apply { setStringProperty(QUEUE_UNIQUE_ID, "1") }
      }
    }

    val size = jmsTemplate.browse(QUEUE_TASKS) { _: Session, browser: QueueBrowser ->
      browser.enumeration.toList().size
    }

    val msg = jmsTemplate.receiveAndConvert(QUEUE_TASKS) as String

    assertThat(msg).isEqualTo("message 5")
    assertThat(size).isEqualTo(1)
  }

  @Test
  fun `when sending messages with some common unique id then messages are deduplicated`() {
    for (i in 1..6) {
      jmsTemplate.convertAndSend(
        QUEUE_TASKS,
        "message $i",
      ) {
        it.apply { setStringProperty(QUEUE_UNIQUE_ID, i.rem(2).toString()) }
      }
    }

    val size = jmsTemplate.browse(QUEUE_TASKS) { _: Session, browser: QueueBrowser ->
      browser.enumeration.toList().size
    }

    val msg = jmsTemplate.receiveAndConvert(QUEUE_TASKS) as String

    assertThat(msg).isEqualTo("message 5")
    assertThat(size).isEqualTo(2)
  }

  @Test
  fun `when sending messages without unique id then messages are not deduplicated`() {
    for (i in 1..5) {
      jmsTemplate.convertAndSend(
        QUEUE_TASKS,
        "message $i",
      )
    }

    val size = jmsTemplate.browse(QUEUE_TASKS) { _: Session, browser: QueueBrowser ->
      browser.enumeration.toList().size
    }

    val msg = jmsTemplate.receiveAndConvert(QUEUE_TASKS) as String

    assertThat(msg).isEqualTo("message 1")
    assertThat(size).isEqualTo(5)
  }

  @Test
  fun `when sending messages with different priority then high priority messages are received first`() {
    for (i in 0..9) {
      jmsTemplate.priority = i
      jmsTemplate.isExplicitQosEnabled = true
      jmsTemplate.convertAndSend(
        QUEUE_TASKS,
        "message A $i",
      )
    }

    for (i in 9 downTo 0) {
      jmsTemplate.priority = i
      jmsTemplate.isExplicitQosEnabled = true
      jmsTemplate.convertAndSend(
        QUEUE_TASKS,
        "message B $i",
      )
    }

    val size = jmsTemplate.browse(QUEUE_TASKS) { _: Session, browser: QueueBrowser ->
      browser.enumeration.toList().size
    }
    assertThat(size).isEqualTo(20)

    for (i in 9 downTo 0) {
      val msgA = jmsTemplate.receiveAndConvert(QUEUE_TASKS) as String
      assertThat(msgA).isEqualTo("message A $i")
      val msgB = jmsTemplate.receiveAndConvert(QUEUE_TASKS) as String
      assertThat(msgB).isEqualTo("message B $i")
    }
  }
}
