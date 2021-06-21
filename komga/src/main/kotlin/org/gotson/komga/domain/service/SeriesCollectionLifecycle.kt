package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.events.EventPublisher
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.infrastructure.image.MosaicGenerator
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SeriesCollectionLifecycle(
  private val collectionRepository: SeriesCollectionRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val mosaicGenerator: MosaicGenerator,
  private val eventPublisher: EventPublisher,
) {

  @Throws(
    DuplicateNameException::class
  )
  fun addCollection(collection: SeriesCollection): SeriesCollection {
    logger.info { "Adding new collection: $collection" }

    if (collectionRepository.existsByName(collection.name))
      throw DuplicateNameException("Collection name already exists")

    collectionRepository.insert(collection)

    eventPublisher.publishEvent(DomainEvent.CollectionAdded(collection))

    return collectionRepository.findByIdOrNull(collection.id)!!
  }

  fun updateCollection(toUpdate: SeriesCollection) {
    logger.info { "Update collection: $toUpdate" }

    val existing = collectionRepository.findByIdOrNull(toUpdate.id)
      ?: throw IllegalArgumentException("Cannot update collection that does not exist")

    if (existing.name != toUpdate.name && collectionRepository.existsByName(toUpdate.name))
      throw DuplicateNameException("Collection name already exists")

    collectionRepository.update(toUpdate)

    eventPublisher.publishEvent(DomainEvent.CollectionUpdated(toUpdate))
  }

  fun deleteCollection(collection: SeriesCollection) {
    collectionRepository.delete(collection.id)
    eventPublisher.publishEvent(DomainEvent.CollectionDeleted(collection))
  }

  fun getThumbnailBytes(collection: SeriesCollection): ByteArray {
    val ids = with(mutableListOf<String>()) {
      while (size < 4) {
        this += collection.seriesIds.take(4)
      }
      this.take(4)
    }

    val images = ids.mapNotNull { seriesLifecycle.getThumbnailBytes(it) }
    return mosaicGenerator.createMosaic(images)
  }
}
