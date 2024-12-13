package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.MetadataPatchTarget
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.persistence.BookMetadataAggregationRepository
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.infrastructure.metadata.SeriesMetadataFromBookProvider
import org.gotson.komga.infrastructure.metadata.SeriesMetadataProvider
import org.gotson.komga.language.mostFrequent
import org.springframework.context.ApplicationEventPublisher
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
  private val collectionLifecycle: SeriesCollectionLifecycle,
  private val eventPublisher: ApplicationEventPublisher,
) {
  fun refreshMetadata(series: Series) {
    logger.info { "Refresh metadata for series: $series" }

    val library = libraryRepository.findById(series.libraryId)
    var changed = false

    seriesMetadataFromBookProviders.forEach { provider ->
      when {
        !(provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.SERIES) || provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.COLLECTION)) ->
          logger.info { "Library is not set to import series or collection metadata for this provider, skipping: ${provider.javaClass.simpleName}" }

        else -> {
          logger.debug { "Provider: ${provider.javaClass.simpleName}" }
          val patches =
            bookRepository
              .findAllBySeriesId(series.id)
              .mapNotNull { book ->
                try {
                  provider.getSeriesMetadataFromBook(BookWithMedia(book, mediaRepository.findById(book.id)), library.importComicInfoSeriesAppendVolume)
                } catch (e: Exception) {
                  logger.error(e) { "Error while getting metadata from ${provider.javaClass.simpleName} for book: $book" }
                  null
                }
              }

          if (provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.SERIES)) {
            handlePatchForSeriesMetadata(patches, series)
            changed = true
          }

          if (provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.COLLECTION)) {
            patches.flatMap { it.collections }.distinct().forEach { collection ->
              collectionLifecycle.addSeriesToCollection(collection, series)
            }
          }
        }
      }
    }

    seriesMetadataProviders.forEach { provider ->
      when {
        !provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.SERIES) ->
          logger.info { "Library is not set to import series metadata for this provider, skipping: ${provider.javaClass.simpleName}" }
        else -> {
          logger.debug { "Provider: ${provider.javaClass.simpleName}" }
          val patch =
            try {
              provider.getSeriesMetadata(series)
            } catch (e: Exception) {
              logger.error(e) { "Error while getting metadata from ${provider::class.simpleName} for series: $series" }
              null
            }

          if (provider.shouldLibraryHandlePatch(library, MetadataPatchTarget.SERIES)) {
            handlePatchForSeriesMetadata(patch, series)
            changed = true
          }
        }
      }
    }

    if (changed) eventPublisher.publishEvent(DomainEvent.SeriesUpdated(series))
  }

  private fun handlePatchForSeriesMetadata(
    patches: List<SeriesMetadataPatch>,
    series: Series,
  ) {
    val aggregatedPatch =
      SeriesMetadataPatch(
        title = patches.mostFrequent { it.title },
        titleSort = patches.mostFrequent { it.titleSort },
        status = patches.mostFrequent { it.status },
        genres =
          patches
            .mapNotNull { it.genres }
            .flatten()
            .toSet()
            .ifEmpty { null },
        language = patches.mostFrequent { it.language },
        summary = null,
        readingDirection = patches.mostFrequent { it.readingDirection },
        ageRating = patches.mapNotNull { it.ageRating }.maxOrNull(),
        publisher = patches.mostFrequent { it.publisher },
        totalBookCount = patches.mapNotNull { it.totalBookCount }.maxOrNull(),
        collections = emptySet(),
      )

    handlePatchForSeriesMetadata(aggregatedPatch, series)
  }

  private fun handlePatchForSeriesMetadata(
    patch: SeriesMetadataPatch?,
    series: Series,
  ) {
    patch?.let { sPatch ->
      seriesMetadataRepository.findById(series.id).let {
        logger.debug { "Apply metadata for series: $series" }

        logger.debug { "Original metadata: $it" }
        logger.debug { "Patch: $sPatch" }
        val patched = metadataApplier.apply(sPatch, it)
        logger.debug { "Patched metadata: $patched" }

        seriesMetadataRepository.update(patched)
      }
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
