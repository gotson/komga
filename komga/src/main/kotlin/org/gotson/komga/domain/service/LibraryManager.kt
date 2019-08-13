package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.persistence.SerieRepository
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.stereotype.Service
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class LibraryManager(
    private val komgaProperties: KomgaProperties,
    private val fileSystemScanner: FileSystemScanner,
    private val serieRepository: SerieRepository
) {
  fun scanRootFolder() {
    logger.info { "Scanning root folder: ${komgaProperties.rootFolder}" }
    measureTimeMillis {
      val series = fileSystemScanner.scanRootFolder(Paths.get(komgaProperties.rootFolder))

      if (series.isNotEmpty())
        serieRepository.saveAll(series)
    }.also { logger.info { "Scan finished in $it ms" } }
  }
}