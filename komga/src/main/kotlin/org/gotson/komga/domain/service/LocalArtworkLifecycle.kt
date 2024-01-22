package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.infrastructure.metadata.localartwork.LocalArtworkProvider
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class LocalArtworkLifecycle(
  private val libraryRepository: LibraryRepository,
  private val bookLifecycle: BookLifecycle,
  private val seriesLifecycle: SeriesLifecycle,
  private val localArtworkProvider: LocalArtworkProvider,
) {
  fun refreshLocalArtwork(book: Book) {
    logger.info { "Refresh local artwork for book: $book" }
    val library = libraryRepository.findById(book.libraryId)

    if (library.importLocalArtwork)
      localArtworkProvider.getBookThumbnails(book).forEach {
        bookLifecycle.addThumbnailForBook(it, if (it.selected) MarkSelectedPreference.IF_NONE_OR_GENERATED else MarkSelectedPreference.NO)
      }
    else
      logger.info { "Library is not set to import local artwork, skipping" }
  }

  fun refreshLocalArtwork(series: Series) {
    logger.info { "Refresh local artwork for series: $series" }
    val library = libraryRepository.findById(series.libraryId)

    if (library.importLocalArtwork)
      localArtworkProvider.getSeriesThumbnails(series).forEach {
        seriesLifecycle.addThumbnailForSeries(it, if (it.selected) MarkSelectedPreference.IF_NONE_OR_GENERATED else MarkSelectedPreference.NO)
      }
    else
      logger.info { "Library is not set to import local artwork, skipping" }
  }
}
