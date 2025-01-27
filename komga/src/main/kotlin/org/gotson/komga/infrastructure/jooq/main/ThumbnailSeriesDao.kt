package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.ThumbnailSeriesRecord
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Component
class ThumbnailSeriesDao(
  private val dsl: DSLContext,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : ThumbnailSeriesRepository {
  private val ts = Tables.THUMBNAIL_SERIES

  override fun findByIdOrNull(thumbnailId: String): ThumbnailSeries? =
    dsl
      .selectFrom(ts)
      .where(ts.ID.eq(thumbnailId))
      .fetchOneInto(ts)
      ?.toDomain()

  override fun findAllBySeriesId(seriesId: String): Collection<ThumbnailSeries> =
    dsl
      .selectFrom(ts)
      .where(ts.SERIES_ID.eq(seriesId))
      .fetchInto(ts)
      .map { it.toDomain() }

  override fun findAllBySeriesIdIdAndType(
    seriesId: String,
    type: ThumbnailSeries.Type,
  ): Collection<ThumbnailSeries> =
    dsl
      .selectFrom(ts)
      .where(ts.SERIES_ID.eq(seriesId))
      .and(ts.TYPE.eq(type.toString()))
      .fetchInto(ts)
      .map { it.toDomain() }

  override fun findSelectedBySeriesIdOrNull(seriesId: String): ThumbnailSeries? =
    dsl
      .selectFrom(ts)
      .where(ts.SERIES_ID.eq(seriesId))
      .and(ts.SELECTED.isTrue)
      .limit(1)
      .fetchInto(ts)
      .map { it.toDomain() }
      .firstOrNull()

  override fun findAllWithoutMetadata(pageable: Pageable): Page<ThumbnailSeries> {
    val query =
      dsl
        .selectFrom(ts)
        .where(ts.FILE_SIZE.eq(0))
        .or(ts.MEDIA_TYPE.eq(""))
        .or(ts.WIDTH.eq(0))
        .or(ts.HEIGHT.eq(0))

    val count = query.count()
    val items =
      query
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(ts)
        .map { it.toDomain() }

    return PageImpl(items, pageable, count.toLong())
  }

  override fun insert(thumbnail: ThumbnailSeries) {
    dsl
      .insertInto(ts)
      .set(ts.ID, thumbnail.id)
      .set(ts.SERIES_ID, thumbnail.seriesId)
      .set(ts.URL, thumbnail.url?.toString())
      .set(ts.THUMBNAIL, thumbnail.thumbnail)
      .set(ts.TYPE, thumbnail.type.toString())
      .set(ts.MEDIA_TYPE, thumbnail.mediaType)
      .set(ts.WIDTH, thumbnail.dimension.width)
      .set(ts.HEIGHT, thumbnail.dimension.height)
      .set(ts.FILE_SIZE, thumbnail.fileSize)
      .set(ts.SELECTED, thumbnail.selected)
      .execute()
  }

  override fun update(thumbnail: ThumbnailSeries) {
    dsl
      .update(ts)
      .set(ts.SERIES_ID, thumbnail.seriesId)
      .set(ts.THUMBNAIL, thumbnail.thumbnail)
      .set(ts.URL, thumbnail.url?.toString())
      .set(ts.SELECTED, thumbnail.selected)
      .set(ts.TYPE, thumbnail.type.toString())
      .set(ts.MEDIA_TYPE, thumbnail.mediaType)
      .set(ts.WIDTH, thumbnail.dimension.width)
      .set(ts.HEIGHT, thumbnail.dimension.height)
      .set(ts.FILE_SIZE, thumbnail.fileSize)
      .where(ts.ID.eq(thumbnail.id))
      .execute()
  }

  override fun updateMetadata(thumbnails: Collection<ThumbnailSeries>) {
    dsl.batched { c ->
      thumbnails.forEach {
        c
          .dsl()
          .update(ts)
          .set(ts.MEDIA_TYPE, it.mediaType)
          .set(ts.WIDTH, it.dimension.width)
          .set(ts.HEIGHT, it.dimension.height)
          .set(ts.FILE_SIZE, it.fileSize)
          .where(ts.ID.eq(it.id))
          .execute()
      }
    }
  }

  @Transactional
  override fun markSelected(thumbnail: ThumbnailSeries) {
    dsl
      .update(ts)
      .set(ts.SELECTED, false)
      .where(ts.SERIES_ID.eq(thumbnail.seriesId))
      .and(ts.ID.ne(thumbnail.id))
      .execute()

    dsl
      .update(ts)
      .set(ts.SELECTED, true)
      .where(ts.SERIES_ID.eq(thumbnail.seriesId))
      .and(ts.ID.eq(thumbnail.id))
      .execute()
  }

  override fun delete(thumbnailSeriesId: String) {
    dsl.deleteFrom(ts).where(ts.ID.eq(thumbnailSeriesId)).execute()
  }

  override fun deleteBySeriesId(seriesId: String) {
    dsl.deleteFrom(ts).where(ts.SERIES_ID.eq(seriesId)).execute()
  }

  @Transactional
  override fun deleteBySeriesIds(seriesIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, seriesIds)

    dsl.deleteFrom(ts).where(ts.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
  }

  private fun ThumbnailSeriesRecord.toDomain() =
    ThumbnailSeries(
      thumbnail = thumbnail,
      url = url?.let { URL(it) },
      selected = selected,
      type = ThumbnailSeries.Type.valueOf(type),
      mediaType = mediaType,
      fileSize = fileSize,
      dimension = Dimension(width, height),
      id = id,
      seriesId = seriesId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate,
    )
}
