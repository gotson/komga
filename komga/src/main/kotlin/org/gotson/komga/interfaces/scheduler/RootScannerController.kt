package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.domain.service.LibraryManager
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

@Controller
class RootScannerController(
    private val libraryManager: LibraryManager
) {

  @EventListener(ApplicationReadyEvent::class)
  @Scheduled(cron = "#{@komgaProperties.rootFolderScanCron ?: '-'}")
  fun scanRootFolder() {
    libraryManager.scanRootFolder()
  }
}