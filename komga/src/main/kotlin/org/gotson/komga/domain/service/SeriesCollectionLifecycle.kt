package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.ThumbnailSeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesCollectionRepository
import org.gotson.komga.infrastructure.image.MosaicGenerator
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class SeriesCollectionLifecycle(
  private val collectionRepository: SeriesCollectionRepository,
  private val thumbnailSeriesCollectionRepository: ThumbnailSeriesCollectionRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val mosaicGenerator: MosaicGenerator,
  private val eventPublisher: ApplicationEventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {
  @Throws(
    DuplicateNameException::class,
  )
  @Transactional
  fun addCollection(collection: SeriesCollection): SeriesCollection {
    logger.info { "Adding new collection: $collection" }

    if (collectionRepository.existsByName(collection.name))
      throw DuplicateNameException("Collection name already exists")

    collectionRepository.insert(collection)

    eventPublisher.publishEvent(DomainEvent.CollectionAdded(collection))

    return collectionRepository.findByIdOrNull(collection.id)!!
  }

  @Transactional
  fun updateCollection(toUpdate: SeriesCollection) {
    logger.info { "Update collection: $toUpdate" }

    val existing =
      collectionRepository.findByIdOrNull(toUpdate.id)
        ?: throw IllegalArgumentException("Cannot update collection that does not exist")

    if (!existing.name.equals(toUpdate.name, true) && collectionRepository.existsByName(toUpdate.name))
      throw DuplicateNameException("Collection name already exists")

    collectionRepository.update(toUpdate)

    eventPublisher.publishEvent(DomainEvent.CollectionUpdated(toUpdate))
  }

  fun deleteCollection(collection: SeriesCollection) {
    transactionTemplate.executeWithoutResult {
      thumbnailSeriesCollectionRepository.deleteByCollectionId(collection.id)
      collectionRepository.delete(collection.id)
    }
    eventPublisher.publishEvent(DomainEvent.CollectionDeleted(collection))
  }

  /**
   * Add series to collection by name.
   * Collection will be created if it doesn't exist.
   */
  @Transactional
  fun addSeriesToCollection(
    collectionName: String,
    series: Series,
  ) {
    collectionRepository.findByNameOrNull(collectionName).let { existing ->
      if (existing != null) {
        if (existing.seriesIds.contains(series.id)) {
          logger.debug { "Series is already in existing collection '${existing.name}'" }
        } else {
          logger.debug { "Adding series '${series.name}' to existing collection '${existing.name}'" }
          updateCollection(
            existing.copy(seriesIds = existing.seriesIds + series.id),
          )
        }
      } else {
        logger.debug { "Adding series '${series.name}' to new collection '$collectionName'" }
        addCollection(
          SeriesCollection(
            name = collectionName,
            seriesIds = listOf(series.id),
          ),
        )
      }
    }
  }

  fun deleteEmptyCollections() {
    logger.info { "Deleting empty collections" }
    val toDelete = collectionRepository.findAllEmpty()
    transactionTemplate.executeWithoutResult {
      thumbnailSeriesCollectionRepository.deleteByCollectionIds(toDelete.map { it.id })
      collectionRepository.delete(toDelete.map { it.id })
    }

    toDelete.forEach { eventPublisher.publishEvent(DomainEvent.CollectionDeleted(it)) }
  }

  fun addThumbnail(thumbnail: ThumbnailSeriesCollection): ThumbnailSeriesCollection {
    when (thumbnail.type) {
      ThumbnailSeriesCollection.Type.USER_UPLOADED -> {
        thumbnailSeriesCollectionRepository.insert(thumbnail)
        if (thumbnail.selected) {
          thumbnailSeriesCollectionRepository.markSelected(thumbnail)
        }
      }
    }

    eventPublisher.publishEvent(DomainEvent.ThumbnailSeriesCollectionAdded(thumbnail))
    return thumbnail
  }

  fun markSelectedThumbnail(thumbnail: ThumbnailSeriesCollection) {
    thumbnailSeriesCollectionRepository.markSelected(thumbnail)
    eventPublisher.publishEvent(DomainEvent.ThumbnailSeriesCollectionAdded(thumbnail.copy(selected = true)))
  }

  fun deleteThumbnail(thumbnail: ThumbnailSeriesCollection) {
    thumbnailSeriesCollectionRepository.delete(thumbnail.id)
    thumbnailsHouseKeeping(thumbnail.collectionId)
    eventPublisher.publishEvent(DomainEvent.ThumbnailSeriesCollectionDeleted(thumbnail))
  }

  fun getThumbnailBytes(thumbnailId: String): ByteArray? = thumbnailSeriesCollectionRepository.findByIdOrNull(thumbnailId)?.thumbnail

  fun getThumbnailBytes(
    collection: SeriesCollection,
    userId: String,
  ): ByteArray {
    thumbnailSeriesCollectionRepository.findSelectedByCollectionIdOrNull(collection.id)?.let {
      return it.thumbnail
    }

    val ids =
      with(mutableListOf<String>()) {
        while (size < 4) {
          this += collection.seriesIds.take(4)
        }
        this.take(4)
      }

    val images = ids.mapNotNull { seriesLifecycle.getThumbnailBytes(it, userId) }
    return mosaicGenerator.createMosaic(images)
  }

  private fun thumbnailsHouseKeeping(collectionId: String) {
    logger.info { "House keeping thumbnails for collection: $collectionId" }
    val all = thumbnailSeriesCollectionRepository.findAllByCollectionId(collectionId)

    val selected = all.filter { it.selected }
    when {
      selected.size > 1 -> {
        logger.info { "More than one thumbnail is selected, removing extra ones" }
        thumbnailSeriesCollectionRepository.markSelected(selected[0])
      }
      selected.isEmpty() && all.isNotEmpty() -> {
        logger.info { "Collection has no selected thumbnail, choosing one automatically" }
        thumbnailSeriesCollectionRepository.markSelected(all.first())
      }
    }
  }
}
