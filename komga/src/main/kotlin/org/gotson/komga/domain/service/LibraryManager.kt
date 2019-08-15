package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SerieRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Service
class LibraryManager(
    private val fileSystemScanner: FileSystemScanner,
    private val serieRepository: SerieRepository,
    private val bookRepository: BookRepository
) {

  @Transactional
  fun scanRootFolder(library: Library) {
    logger.info { "Scanning ${library.name}'s root folder: ${library.root}" }
    measureTimeMillis {
      val series = fileSystemScanner.scanRootFolder(library.fileSystem.getPath(library.root))

      // delete series that don't exist anymore
      if (series.isEmpty())
        serieRepository.deleteAll()
      else
        serieRepository.deleteAllByUrlNotIn(series.map { it.url })

      // match IDs for existing entities
      series.forEach { newSerie ->
        serieRepository.findByUrl(newSerie.url)?.let { existingSerie ->
          newSerie.id = existingSerie.id
          newSerie.books.forEach { newBook ->
            bookRepository.findByUrl(newBook.url)?.let { existingBook ->
              newBook.id = existingBook.id
            }
          }
        }
      }

      serieRepository.saveAll(series)

    }.also { logger.info { "Scan finished in $it ms" } }
  }
}