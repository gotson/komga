package org.gotson.komga.infrastructure.security.session

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.session.events.AbstractSessionEvent
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SessionListener {
  @EventListener
  fun sessionEventLogging(event: AbstractSessionEvent) {
    logger.debug { "${event.javaClass.simpleName}: ${event.sessionId}" }
  }
}
