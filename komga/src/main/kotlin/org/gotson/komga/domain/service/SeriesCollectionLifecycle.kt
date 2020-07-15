package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SeriesCollectionLifecycle(
  private val collectionRepository: SeriesCollectionRepository
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

    collectionRepository.update(toUpdate)
  }

  fun deleteCollection(collectionId: String) {
    collectionRepository.delete(collectionId)
  }
}
