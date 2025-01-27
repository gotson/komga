package org.gotson.komga.interfaces.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.tasks.LOWEST_PRIORITY
import org.gotson.komga.application.tasks.TaskEmitter
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class ThumbnailMetadataFixerController(
  private val taskEmitter: TaskEmitter,
) {
  @EventListener(ApplicationReadyEvent::class)
  fun findThumbnailsWithoutMetadata() {
    logger.info { "Find and fix thumbnails without metadata" }
    taskEmitter.fixThumbnailsWithoutMetadata(LOWEST_PRIORITY)
  }
}
