package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.events.EventPublisher
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.persistence.BookMetadataAggregationRepository
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.infrastructure.metadata.comicrack.ComicInfoProvider
import org.gotson.komga.infrastructure.metadata.epub.EpubMetadataProvider
import org.gotson.komga.infrastructure.metadata.mylar.MylarSeriesProvider
import org.gotson.komga.language.mostFrequent
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SeriesMetadataLifecycle(
  private val seriesMetadataFromBookProviders: List<SeriesMetadataFromBookProvider>,
  private val seriesMetadataProviders: List<SeriesMetadataProvider>,
  private val metadataApplier: MetadataApplier,
  private val metadataAggregator: MetadataAggregator,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val bookMetadataAggregationRepository: BookMetadataAggregationRepository,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val collectionLifecycle: SeriesCollectionLifecycle,
  private val eventPublisher: EventPublisher,
) {

  fun refreshMetadata(series: Series) {
    logger.info { "Refresh metadata for series: $series" }

    val library = libraryRepository.findById(series.libraryId)
    var changed = false

    seriesMetadataFromBookProviders.forEach { provider ->
      when {
        provider is ComicInfoProvider && !library.importComicInfoSeries && !library.importComicInfoCollection -> logger.info { "Library is not set to import series and collection metadata from ComicInfo, skipping" }
        provider is EpubMetadataProvider && !library.importEpubSeries -> logger.info { "Library is not set to import series metadata from Epub, skipping" }
        else -> {
          logger.debug { "Provider: ${provider.javaClass.simpleName}" }
          val patches = bookRepository.findAllBySeriesId(series.id)
            .mapNotNull { provider.getSeriesMetadataFromBook(BookWithMedia(it, mediaRepository.findById(it.id))) }

          if (
            (provider is ComicInfoProvider && library.importComicInfoSeries) ||
            (provider is EpubMetadataProvider && library.importEpubSeries)
          ) {
            handlePatchForSeriesMetadata(patches, series)
            changed = true
          }

          if (provider is ComicInfoProvider && library.importComicInfoCollection) {
            handlePatchForCollections(patches, series)
          }
        }
      }
    }

    seriesMetadataProviders.forEach { provider ->
      when {
        provider is MylarSeriesProvider && !library.importMylarSeries -> logger.info { "Library is not set to import series metadata from Mylar, skipping" }
        else -> {
          logger.debug { "Provider: ${provider.javaClass.simpleName}" }
          val patch = provider.getSeriesMetadata(series)

          if (provider is MylarSeriesProvider && library.importMylarSeries && patch != null) {
            handlePatchForSeriesMetadata(patch, series)
            changed = true
          }
        }
      }
    }

    if (changed) eventPublisher.publishEvent(DomainEvent.SeriesUpdated(series))
  }

  private fun handlePatchForCollections(
    patches: List<SeriesMetadataPatch>,
    series: Series,
  ) {
    patches.flatMap { it.collections }.distinct().forEach { collection ->
      collectionRepository.findByNameOrNull(collection).let { existing ->
        if (existing != null) {
          if (existing.seriesIds.contains(series.id))
            logger.debug { "Series is already in existing collection '${existing.name}'" }
          else {
            logger.debug { "Adding series '${series.name}' to existing collection '${existing.name}'" }
            collectionLifecycle.updateCollection(
              existing.copy(seriesIds = existing.seriesIds + series.id),
            )
          }
        } else {
          logger.debug { "Adding series '${series.name}' to new collection '$collection'" }
          collectionLifecycle.addCollection(
            SeriesCollection(
              name = collection,
              seriesIds = listOf(series.id),
            ),
          )
        }
      }
    }
  }

  private fun handlePatchForSeriesMetadata(
    patches: List<SeriesMetadataPatch>,
    series: Series,
  ) {
    val aggregatedPatch = SeriesMetadataPatch(
      title = patches.mostFrequent { it.title },
      titleSort = patches.mostFrequent { it.titleSort },
      status = patches.mostFrequent { it.status },
      genres = patches.mapNotNull { it.genres }.flatten().toSet().ifEmpty { null },
      language = patches.mostFrequent { it.language },
      summary = null,
      readingDirection = patches.mostFrequent { it.readingDirection },
      ageRating = patches.mapNotNull { it.ageRating }.maxOrNull(),
      publisher = patches.mostFrequent { it.publisher },
      totalBookCount = patches.mapNotNull { it.totalBookCount }.maxOrNull(),
      collections = emptyList(),
    )

    handlePatchForSeriesMetadata(aggregatedPatch, series)
  }

  private fun handlePatchForSeriesMetadata(
    patch: SeriesMetadataPatch,
    series: Series,
  ) {
    seriesMetadataRepository.findById(series.id).let {
      logger.debug { "Apply metadata for series: $series" }

      logger.debug { "Original metadata: $it" }
      logger.debug { "Patch: $patch" }
      val patched = metadataApplier.apply(patch, it)
      logger.debug { "Patched metadata: $patched" }

      seriesMetadataRepository.update(patched)
    }
  }

  fun aggregateMetadata(series: Series) {
    logger.info { "Aggregate book metadata for series: $series" }

    val metadatas = bookMetadataRepository.findAllByIds(bookRepository.findAllIdsBySeriesId(series.id))
    val aggregation = metadataAggregator.aggregate(metadatas).copy(seriesId = series.id)

    bookMetadataAggregationRepository.update(aggregation)

    eventPublisher.publishEvent(DomainEvent.SeriesUpdated(series))
  }
}
