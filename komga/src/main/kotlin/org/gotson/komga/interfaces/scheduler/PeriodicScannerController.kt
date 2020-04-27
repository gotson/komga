package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Controller
class PeriodicScannerController(
  private val taskReceiver: TaskReceiver
) {

  @EventListener(classes = [ApplicationReadyEvent::class], condition = "@komgaProperties.librariesScanStartup")
  @Scheduled(cron = "#{@komgaProperties.librariesScanCron ?: '-'}")
  fun scanAllLibraries() {
    logger.info { "Periodic libraries scan starting" }
    taskReceiver.scanLibraries()
  }
}
