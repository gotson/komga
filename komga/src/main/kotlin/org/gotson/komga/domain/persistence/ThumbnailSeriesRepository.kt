package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ThumbnailSeries

interface ThumbnailSeriesRepository {
  fun findBySeriesId(seriesId: String): Collection<ThumbnailSeries>
  fun findSelectedBySeriesId(seriesId: String): ThumbnailSeries?

  fun insert(thumbnail: ThumbnailSeries)
  fun markSelected(thumbnail: ThumbnailSeries)

  fun delete(thumbnailSeriesId: String)
  fun deleteBySeriesId(seriesId: String)
  fun deleteBySeriesIds(seriesIds: Collection<String>)
}
