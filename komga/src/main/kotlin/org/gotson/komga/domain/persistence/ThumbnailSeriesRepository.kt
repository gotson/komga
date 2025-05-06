package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ThumbnailSeries

interface ThumbnailSeriesRepository {
  fun findByIdOrNull(thumbnailId: String): ThumbnailSeries?

  fun findSelectedBySeriesIdOrNull(seriesId: String): ThumbnailSeries?

  fun findAllBySeriesId(seriesId: String): Collection<ThumbnailSeries>

  fun findAllBySeriesIdIdAndType(
    seriesId: String,
    type: ThumbnailSeries.Type,
  ): Collection<ThumbnailSeries>

  fun insert(thumbnail: ThumbnailSeries)

  fun update(thumbnail: ThumbnailSeries)

  fun markSelected(thumbnail: ThumbnailSeries)

  fun delete(thumbnailSeriesId: String)

  fun deleteBySeriesId(seriesId: String)

  fun deleteBySeriesIds(seriesIds: Collection<String>)
}
