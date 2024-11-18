package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ThumbnailSeries
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ThumbnailSeriesRepository {
  fun findByIdOrNull(thumbnailId: String): ThumbnailSeries?

  fun findSelectedBySeriesIdOrNull(seriesId: String): ThumbnailSeries?

  fun findAllBySeriesId(seriesId: String): Collection<ThumbnailSeries>

  fun findAllBySeriesIdIdAndType(
    seriesId: String,
    type: ThumbnailSeries.Type,
  ): Collection<ThumbnailSeries>

  fun findAllWithoutMetadata(pageable: Pageable): Page<ThumbnailSeries>

  fun insert(thumbnail: ThumbnailSeries)

  fun update(thumbnail: ThumbnailSeries)

  fun updateMetadata(thumbnails: Collection<ThumbnailSeries>)

  fun markSelected(thumbnail: ThumbnailSeries)

  fun delete(thumbnailSeriesId: String)

  fun deleteBySeriesId(seriesId: String)

  fun deleteBySeriesIds(seriesIds: Collection<String>)
}
