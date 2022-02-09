package org.gotson.komga.application.events

import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.infrastructure.jms.JMS_PROPERTY_TYPE
import org.gotson.komga.infrastructure.jms.TOPIC_EVENTS
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
    jmsTemplate.convertAndSend(TOPIC_EVENTS, event) {
      it.apply {
        setStringProperty(JMS_PROPERTY_TYPE, event.javaClass.simpleName)
      }
    }
  }
}
