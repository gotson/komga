package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.gotson.komga.domain.persistence.SerieRepository
import org.gotson.komga.domain.service.FileSystemScanner
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Controller
class RootScannerController(
    private val komgaProperties: KomgaProperties,
    private val fileSystemScanner: FileSystemScanner,
    private val serieRepository: SerieRepository
) {

  @EventListener(ApplicationReadyEvent::class)
  @Scheduled(cron = "#{@komgaProperties.rootFolderScanCron ?: '-'}")
  fun scanRootFolder() {
    logger.info { "Scanning root folder: ${komgaProperties.rootFolder}" }
    measureTimeMillis {
      val series = fileSystemScanner.scanRootFolder(komgaProperties.rootFolder)

      if (series.isNotEmpty())
        serieRepository.saveAll(series)
    }.also { logger.info { "Scan finished in $it ms" } }
  }
}