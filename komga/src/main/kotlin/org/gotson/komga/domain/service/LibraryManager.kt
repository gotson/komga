package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.SerieRepository
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class LibraryManager(
    private val fileSystemScanner: FileSystemScanner,
    private val serieRepository: SerieRepository
) {
  fun scanRootFolder(library: Library) {
    logger.info { "Scanning ${library.name}'s root folder: ${library.root}" }
    measureTimeMillis {
      val series = fileSystemScanner.scanRootFolder(library.fileSystem.getPath(library.root))

      if (series.isNotEmpty())
        serieRepository.saveAll(series)
    }.also { logger.info { "Scan finished in $it ms" } }
  }
}