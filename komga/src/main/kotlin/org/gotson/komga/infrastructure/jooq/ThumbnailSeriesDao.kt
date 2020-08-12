package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.ThumbnailSeriesRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.net.URL

@Component
class ThumbnailSeriesDao(
  private val dsl: DSLContext
) : ThumbnailSeriesRepository {
  private val ts = Tables.THUMBNAIL_SERIES

  override fun findBySeriesId(seriesId: String): Collection<ThumbnailSeries> =
    dsl.selectFrom(ts)
      .where(ts.SERIES_ID.eq(seriesId))
      .fetchInto(ts)
      .map { it.toDomain() }

  override fun findSelectedBySeriesId(seriesId: String): ThumbnailSeries? =
    dsl.selectFrom(ts)
      .where(ts.SERIES_ID.eq(seriesId))
      .and(ts.SELECTED.isTrue)
      .limit(1)
      .fetchInto(ts)
      .map { it.toDomain() }
      .firstOrNull()

  override fun insert(thumbnail: ThumbnailSeries) {
    dsl.insertInto(ts)
      .set(ts.ID, thumbnail.id)
      .set(ts.SERIES_ID, thumbnail.seriesId)
      .set(ts.URL, thumbnail.url.toString())
      .set(ts.SELECTED, thumbnail.selected)
      .execute()
  }

  override fun markSelected(thumbnail: ThumbnailSeries) {
    dsl.transaction { config ->
      config.dsl().update(ts)
        .set(ts.SELECTED, false)
        .where(ts.SERIES_ID.eq(thumbnail.seriesId))
        .and(ts.ID.ne(thumbnail.id))
        .execute()

      config.dsl().update(ts)
        .set(ts.SELECTED, true)
        .where(ts.SERIES_ID.eq(thumbnail.seriesId))
        .and(ts.ID.eq(thumbnail.id))
        .execute()
    }
  }

  override fun delete(thumbnailSeriesId: String) {
    dsl.deleteFrom(ts).where(ts.ID.eq(thumbnailSeriesId)).execute()
  }

  override fun deleteBySeriesId(seriesId: String) {
    dsl.deleteFrom(ts).where(ts.SERIES_ID.eq(seriesId)).execute()
  }

  override fun deleteBySeriesIds(seriesIds: Collection<String>) {
    dsl.deleteFrom(ts).where(ts.SERIES_ID.`in`(seriesIds)).execute()
  }

  private fun ThumbnailSeriesRecord.toDomain() =
    ThumbnailSeries(
      url = URL(url),
      selected = selected,
      id = id,
      seriesId = seriesId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )
}
