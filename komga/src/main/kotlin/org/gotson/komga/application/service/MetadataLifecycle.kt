package org.gotson.komga.application.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.MetadataApplier
import org.gotson.komga.infrastructure.metadata.comicinfo.ComicInfoProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class MetadataLifecycle(
  private val comicInfoProvider: ComicInfoProvider,
  private val metadataApplier: MetadataApplier,
  private val bookRepository: BookRepository,
  private val seriesRepository: SeriesRepository
) {

  @Transactional
  @Async("refreshMetadataTaskExecutor")
  fun refreshMetadata(book: Book) {
    logger.info { "Refresh metadata for book: $book" }
    val loadedBook = bookRepository.findByIdOrNull(book.id)

    loadedBook?.let { bookToPatch ->
      val patch = comicInfoProvider.getBookMetadataFromBook(bookToPatch)

      patch?.let { bPatch ->
        metadataApplier.apply(bPatch, bookToPatch)
        bookRepository.save(bookToPatch)

        bPatch.series?.let { sPatch ->
          metadataApplier.apply(sPatch, bookToPatch.series)
          seriesRepository.save(bookToPatch.series)
        }
      }
    }
  }

}
