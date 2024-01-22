package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ThumbnailSeriesCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ThumbnailSeriesCollectionRepository {
  fun findByIdOrNull(thumbnailId: String): ThumbnailSeriesCollection?

  fun findSelectedByCollectionIdOrNull(collectionId: String): ThumbnailSeriesCollection?

  fun findAllByCollectionId(collectionId: String): Collection<ThumbnailSeriesCollection>

  fun findAllWithoutMetadata(pageable: Pageable): Page<ThumbnailSeriesCollection>

  fun insert(thumbnail: ThumbnailSeriesCollection)

  fun update(thumbnail: ThumbnailSeriesCollection)

  fun updateMetadata(thumbnails: Collection<ThumbnailSeriesCollection>)

  fun markSelected(thumbnail: ThumbnailSeriesCollection)

  fun delete(thumbnailCollectionId: String)

  fun deleteByCollectionId(collectionId: String)

  fun deleteByCollectionIds(collectionIds: Collection<String>)
}
