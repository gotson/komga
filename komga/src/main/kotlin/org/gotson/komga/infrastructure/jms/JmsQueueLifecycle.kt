package org.gotson.komga.infrastructure.jms

import mu.KotlinLogging
import org.apache.activemq.artemis.api.core.management.QueueControl
import org.apache.activemq.artemis.api.core.management.ResourceNames
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class JmsQueueLifecycle(
  embeddedActiveMQ: EmbeddedActiveMQ,
) {

  private val queueControl = embeddedActiveMQ.activeMQServer.managementService.getResource("${ResourceNames.QUEUE}$QUEUE_TASKS") as QueueControl

  fun emptyTaskQueue(): Int =
    queueControl.removeAllMessages()
      .also { logger.info { "Removed $it messages from $QUEUE_TASKS" } }
}
