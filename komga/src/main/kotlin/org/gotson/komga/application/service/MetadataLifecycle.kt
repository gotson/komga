package org.gotson.komga.application.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.MetadataApplier
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class MetadataLifecycle(
  private val bookMetadataProviders: List<BookMetadataProvider>,
  private val metadataApplier: MetadataApplier,
  private val bookRepository: BookRepository,
  private val seriesRepository: SeriesRepository
) {

  fun refreshMetadata(book: Book) {
    logger.info { "Refresh metadata for book: $book" }
    bookMetadataProviders.forEach {
      it.getBookMetadataFromBook(book)?.let { bPatch ->
        metadataApplier.apply(bPatch, book)
        bookRepository.save(book)

        bPatch.series?.let { sPatch ->
          metadataApplier.apply(sPatch, book.series)
          seriesRepository.save(book.series)
        }
      }
    }
  }

}
