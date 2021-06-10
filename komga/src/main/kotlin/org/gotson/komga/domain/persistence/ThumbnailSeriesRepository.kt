package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ThumbnailSeries

interface ThumbnailSeriesRepository {
  fun findSelectedBySeriesIdOrNull(seriesId: String): ThumbnailSeries?

  fun findAllBySeriesId(seriesId: String): Collection<ThumbnailSeries>

  fun insert(thumbnail: ThumbnailSeries)
  fun markSelected(thumbnail: ThumbnailSeries)

  fun delete(thumbnailSeriesId: String)
  fun deleteBySeriesId(seriesId: String)
  fun deleteBySeriesIds(seriesIds: Collection<String>)
}
