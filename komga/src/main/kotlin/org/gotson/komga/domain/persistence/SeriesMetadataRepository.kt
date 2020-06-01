package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.SeriesMetadata

interface SeriesMetadataRepository {
  fun findById(seriesId: Long): SeriesMetadata
  fun findByIdOrNull(seriesId: Long): SeriesMetadata?

  fun insert(metadata: SeriesMetadata): SeriesMetadata
  fun update(metadata: SeriesMetadata)

  fun delete(seriesId: Long)

  fun count(): Long
}
