package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.domain.service.AsyncOrchestrator
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.util.concurrent.RejectedExecutionException

private val logger = KotlinLogging.logger {}

@Profile("dev", "prod")
@Controller
class PeriodicScannerController(
    private val asyncOrchestrator: AsyncOrchestrator
) {

  @EventListener(ApplicationReadyEvent::class)
  @Scheduled(cron = "#{@komgaProperties.librariesScanCron ?: '-'}")
  fun scanRootFolder() {
    try {
      asyncOrchestrator.scanAndParse()
    } catch (e: RejectedExecutionException) {
      logger.warn { "Another scan is already running, skipping" }
    }
  }

  @EventListener(ApplicationReadyEvent::class)
  fun regenerateMissingThumbnails() {
    try {
      asyncOrchestrator.regenerateMissingThumbnails()
    } catch (e: RejectedExecutionException) {
      logger.warn { "Thumbnail regeneration task is already running" }
    }
  }
}
