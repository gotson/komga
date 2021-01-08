package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class PeriodicTrashBinEmptierController(
  private val taskReceiver: TaskReceiver
) {

  @Scheduled(cron = "#{@komgaProperties.trashEmptyingCron ?: '-'}")
  fun emptyTrashBin() {
    logger.info { "Emptying trash bin" }
    taskReceiver.emptyTrash()
  }
}
