package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.service.AsyncOrchestrator
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.util.concurrent.RejectedExecutionException

private val logger = KotlinLogging.logger {}

@Profile("dev", "prod")
@Controller
class RootScannerController(
    private val asyncOrchestrator: AsyncOrchestrator,
    private val komgaProperties: KomgaProperties
) {

  @EventListener(ApplicationReadyEvent::class)
  @Scheduled(cron = "#{@komgaProperties.rootFolderScanCron ?: '-'}")
  fun scanRootFolder() {
    try {
      asyncOrchestrator.scanAndParse(Library("default", komgaProperties.rootFolder))
    } catch (e: RejectedExecutionException) {
      logger.warn { "Another scan is already running, skipping" }
    }
  }
}