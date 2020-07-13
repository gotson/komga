package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.SeriesMetadata

interface SeriesMetadataRepository {
  fun findById(seriesId: String): SeriesMetadata
  fun findByIdOrNull(seriesId: String): SeriesMetadata?

  fun insert(metadata: SeriesMetadata): SeriesMetadata
  fun update(metadata: SeriesMetadata)

  fun delete(seriesId: String)

  fun count(): Long
}
