package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskEmitter
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class PeriodicScannerController(
  private val taskEmitter: TaskEmitter,
) {

  @EventListener(classes = [ApplicationReadyEvent::class], condition = "@komgaProperties.librariesScanStartup")
  @Scheduled(cron = "#{@komgaProperties.librariesScanCron ?: '-'}")
  fun scanAllLibraries() {
    logger.info { "Periodic libraries scan starting" }
    taskEmitter.scanLibraries()
  }
}
