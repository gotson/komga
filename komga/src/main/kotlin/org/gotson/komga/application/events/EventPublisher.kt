package org.gotson.komga.application.events

import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.infrastructure.jms.QUEUE_SSE
import org.gotson.komga.infrastructure.jms.QUEUE_SSE_TYPE
import org.gotson.komga.infrastructure.jms.QUEUE_TYPE
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import javax.jms.ConnectionFactory

@Service
class EventPublisher(
  connectionFactory: ConnectionFactory,
) {
  private val jmsTemplate = JmsTemplate(connectionFactory).apply {
    isPubSubDomain = true
  }

  fun publishEvent(event: DomainEvent) {
    jmsTemplate.convertAndSend(QUEUE_SSE, event) {
      it.apply {
        setStringProperty(QUEUE_TYPE, QUEUE_SSE_TYPE)
      }
    }
  }
}
