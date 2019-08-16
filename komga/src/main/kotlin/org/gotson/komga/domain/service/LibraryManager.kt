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
    logger.info { "Updating library: ${library.name}, root folder: ${library.root}" }
    measureTimeMillis {
      val series = fileSystemScanner.scanRootFolder(library.fileSystem.getPath(library.root))

      // delete series that don't exist anymore
      if (series.isEmpty()) {
        logger.info { "Scan returned no series, deleting all existing series" }
        serieRepository.deleteAll()
      } else {
        val urls = series.map { it.url }
        val countOfSeriesToDelete = serieRepository.countByUrlNotIn(urls)
        if (countOfSeriesToDelete > 0) {
          logger.info { "Deleting $countOfSeriesToDelete series not on disk anymore" }
          serieRepository.deleteAllByUrlNotIn(urls)
        }
      }

      // match IDs for existing entities
      series.forEach { newSerie ->
        serieRepository.findByUrl(newSerie.url)?.let { existingSerie ->
          newSerie.id = existingSerie.id
          newSerie.books.forEach { newBook ->
            bookRepository.findByUrl(newBook.url)?.let { existingBook ->
              newBook.id = existingBook.id
              // conserve metadata if book has not changed
              if (newBook.updated == existingBook.updated)
                newBook.metadata = existingBook.metadata
              else
                logger.info { "Book changed on disk, reset metadata status: ${newBook.url}" }
            }
          }
        }
      }

      serieRepository.saveAll(series)

    }.also { logger.info { "Update finished in $it ms" } }
  }
}