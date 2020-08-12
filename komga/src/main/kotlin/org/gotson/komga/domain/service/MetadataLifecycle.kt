package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.infrastructure.metadata.BookMetadataProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.metadata.comicinfo.ComicInfoProvider
import org.gotson.komga.infrastructure.metadata.epub.EpubMetadataProvider
import org.gotson.komga.infrastructure.metadata.localmediaassets.LocalMediaAssetsProvider
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class MetadataLifecycle(
  private val bookMetadataProviders: List<BookMetadataProvider>,
  private val seriesMetadataProviders: List<SeriesMetadataProvider>,
  private val metadataApplier: MetadataApplier,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val collectionRepository: SeriesCollectionRepository,
  private val collectionLifecycle: SeriesCollectionLifecycle,
  private val localMediaAssetsProvider: LocalMediaAssetsProvider
) {

  fun refreshMetadata(book: Book) {
    logger.info { "Refresh metadata for book: $book" }
    val media = mediaRepository.findById(book.id)

    val library = libraryRepository.findById(book.libraryId)

    bookMetadataProviders.forEach { provider ->
      when {
        provider is ComicInfoProvider && !library.importComicInfoBook -> logger.info { "Library is not set to import book metadata from ComicInfo, skipping" }
        provider is EpubMetadataProvider && !library.importEpubBook -> logger.info { "Library is not set to import book metadata from Epub, skipping" }
        else -> {
          logger.debug { "Provider: $provider" }
          provider.getBookMetadataFromBook(book, media)?.let { bPatch ->

            bookMetadataRepository.findById(book.id).let {
              logger.debug { "Original metadata: $it" }
              val patched = metadataApplier.apply(bPatch, it)
              logger.debug { "Patched metadata: $patched" }

              bookMetadataRepository.update(patched)
            }
          }
        }
      }
    }

    localMediaAssetsProvider.getBookThumbnails(book).forEach {
      bookLifecycle.addThumbnailForBook(it)
    }
  }

  fun refreshMetadata(series: Series) {
    logger.info { "Refresh metadata for series: $series" }

    val library = libraryRepository.findById(series.libraryId)

    seriesMetadataProviders.forEach { provider ->
      when {
        provider is ComicInfoProvider && !library.importComicInfoSeries && !library.importComicInfoCollection -> logger.info { "Library is not set to import series and collection metadata from ComicInfo, skipping" }
        provider is EpubMetadataProvider && !library.importEpubSeries -> logger.info { "Library is not set to import series metadata from Epub, skipping" }
        else -> {
          logger.debug { "Provider: $provider" }
          val patches = bookRepository.findBySeriesId(series.id)
            .mapNotNull { provider.getSeriesMetadataFromBook(it, mediaRepository.findById(it.id)) }

          // handle series metadata
          if ((provider is ComicInfoProvider && library.importComicInfoSeries) ||
            (provider is EpubMetadataProvider && !library.importEpubSeries)) {
            val title = patches.uniqueOrNull { it.title }
            val titleSort = patches.uniqueOrNull { it.titleSort }
            val status = patches.uniqueOrNull { it.status }

            if (title == null) logger.debug { "Ignoring title, values are not unique within series books" }
            if (titleSort == null) logger.debug { "Ignoring sort title, values are not unique within series books" }
            if (status == null) logger.debug { "Ignoring status, values are not unique within series books" }

            val aggregatedPatch = SeriesMetadataPatch(title, titleSort, status)

            seriesMetadataRepository.findById(series.id).let {
              logger.debug { "Apply metadata for series: $series" }

              logger.debug { "Original metadata: $it" }
              val patched = metadataApplier.apply(aggregatedPatch, it)
              logger.debug { "Patched metadata: $patched" }

              seriesMetadataRepository.update(patched)
            }
          }

          // add series to collections
          if (provider is ComicInfoProvider && library.importComicInfoCollection) {
            patches.flatMap { it.collections }.distinct().forEach { collection ->
              collectionRepository.findByNameOrNull(collection).let { existing ->
                if (existing != null) {
                  if (existing.seriesIds.contains(series.id))
                    logger.debug { "Series is already in existing  collection ${existing.name}" }
                  else {
                    logger.debug { "Adding series ${series.name} to existing collection ${existing.name}" }
                    collectionLifecycle.updateCollection(
                      existing.copy(seriesIds = existing.seriesIds + series.id)
                    )
                  }
                } else {
                  logger.debug { "Adding series ${series.name} to new collection $collection" }
                  collectionLifecycle.addCollection(SeriesCollection(
                    name = collection,
                    seriesIds = listOf(series.id)
                  ))
                }
              }
            }
          }
        }
      }
    }
  }

  private fun <T, R : Any> Iterable<T>.uniqueOrNull(transform: (T) -> R?): R? {
    return this
      .mapNotNull(transform)
      .distinct()
      .let {
        if (it.size == 1) it.first() else null
      }
  }
}

