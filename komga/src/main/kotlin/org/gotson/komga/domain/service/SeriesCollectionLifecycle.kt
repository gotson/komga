package org.gotson.komga.domain.service

import mu.KotlinLogging
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
  private val mosaicGenerator: MosaicGenerator
) {

  @Throws(
    DuplicateNameException::class
  )
  fun addCollection(collection: SeriesCollection): SeriesCollection {
    logger.info { "Adding new collection: $collection" }

    if (collectionRepository.existsByName(collection.name))
      throw DuplicateNameException("Collection name already exists")

    collectionRepository.insert(collection)

    return collectionRepository.findByIdOrNull(collection.id)!!
  }

  fun updateCollection(toUpdate: SeriesCollection) {
    val existing = collectionRepository.findByIdOrNull(toUpdate.id)
      ?: throw IllegalArgumentException("Cannot update collection that does not exist")

    if (existing.name != toUpdate.name && collectionRepository.existsByName(toUpdate.name))
      throw DuplicateNameException("Collection name already exists")
    val allSeriesIds = toUpdate.seriesIds.toMutableList()
    collectionRepository.findDeletedSeriesByName(toUpdate.name).let { deleted ->
      deleted.forEach { (number, seriesId) ->
        if (allSeriesIds.size < number) allSeriesIds.add(seriesId)
        else allSeriesIds.add(number, seriesId)
      }
    }

    collectionRepository.update(toUpdate.copy(seriesIds = allSeriesIds))
  }

  fun deleteCollection(collectionId: String) {
    collectionRepository.delete(collectionId)
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
