package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class MetadataLifecycle(
  private val bookMetadataProviders: List<BookMetadataProvider>,
  private val metadataApplier: MetadataApplier,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository
) {

  fun refreshMetadata(book: Book) {
    logger.info { "Refresh metadata for book: $book" }
    val media = mediaRepository.findById(book.id)

    bookMetadataProviders.forEach { provider ->
      provider.getBookMetadataFromBook(book, media)?.let { bPatch ->

        bookMetadataRepository.findById(book.id).let {
          logger.debug { "Original metadata: $it" }
          val patched = metadataApplier.apply(bPatch, it)
          logger.debug { "Patched metadata: $patched" }

          bookMetadataRepository.update(patched)
        }

        bPatch.series?.let { sPatch ->
          seriesMetadataRepository.findById(book.seriesId).let {
            logger.debug { "Apply metadata for series: ${book.seriesId}" }

            logger.debug { "Original metadata: $it" }
            val patched = metadataApplier.apply(sPatch, it)
            logger.debug { "Patched metadata: $patched" }

            seriesMetadataRepository.update(patched)
          }
        }
      }
    }
  }

}
