package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.service.LibraryManager
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Controller
class RootScannerController(
    private val libraryManager: LibraryManager,
    private val komgaProperties: KomgaProperties
) {

  @EventListener(ApplicationReadyEvent::class)
  @Scheduled(cron = "#{@komgaProperties.rootFolderScanCron ?: '-'}")
  fun scanRootFolder() {
    logger.info { "Starting periodic library scan" }
    libraryManager.scanRootFolder(Library("default", komgaProperties.rootFolder))

    logger.info { "Starting periodic book parsing" }
    libraryManager.parseUnparsedBooks()
  }
}