package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Controller
class PeriodicDatabaseBackupController(
  private val taskReceiver: TaskReceiver,
  private val komgaProperties: KomgaProperties
) {

  @EventListener(classes = [ApplicationReadyEvent::class], condition = "@komgaProperties.databaseBackup.startup")
  @Scheduled(cron = "#{@komgaProperties.databaseBackup.schedule ?: '-'}")
  fun scanAllLibraries() {
    if (komgaProperties.databaseBackup.enabled) {
      logger.info { "Periodic database backup" }
      taskReceiver.databaseBackup()
    }
  }
}
