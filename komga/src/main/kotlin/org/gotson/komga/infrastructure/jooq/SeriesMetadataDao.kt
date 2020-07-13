package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.SeriesMetadataRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SeriesMetadataDao(
  private val dsl: DSLContext
) : SeriesMetadataRepository {

  private val d = Tables.SERIES_METADATA

  override fun findById(seriesId: String): SeriesMetadata =
    findOne(seriesId).toDomain()

  override fun findByIdOrNull(seriesId: String): SeriesMetadata? =
    findOne(seriesId)?.toDomain()

  private fun findOne(seriesId: String) =
    dsl.selectFrom(d)
      .where(d.SERIES_ID.eq(seriesId))
      .fetchOneInto(d)

  override fun insert(metadata: SeriesMetadata): SeriesMetadata {
    dsl.insertInto(d)
      .set(d.SERIES_ID, metadata.seriesId)
      .set(d.STATUS, metadata.status.toString())
      .set(d.TITLE, metadata.title)
      .set(d.TITLE_SORT, metadata.titleSort)
      .set(d.STATUS_LOCK, metadata.statusLock)
      .set(d.TITLE_LOCK, metadata.titleLock)
      .set(d.TITLE_SORT_LOCK, metadata.titleSortLock)
      .execute()

    return findById(metadata.seriesId)
  }

  override fun update(metadata: SeriesMetadata) {
    dsl.update(d)
      .set(d.STATUS, metadata.status.toString())
      .set(d.TITLE, metadata.title)
      .set(d.TITLE_SORT, metadata.titleSort)
      .set(d.STATUS_LOCK, metadata.statusLock)
      .set(d.TITLE_LOCK, metadata.titleLock)
      .set(d.TITLE_SORT_LOCK, metadata.titleSortLock)
      .set(d.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(d.SERIES_ID.eq(metadata.seriesId))
      .execute()
  }

  override fun delete(seriesId: String) {
    dsl.deleteFrom(d)
      .where(d.SERIES_ID.eq(seriesId))
      .execute()
  }

  override fun count(): Long = dsl.fetchCount(d).toLong()


  private fun SeriesMetadataRecord.toDomain() =
    SeriesMetadata(
      status = SeriesMetadata.Status.valueOf(status),
      title = title,
      titleSort = titleSort,
      seriesId = seriesId,
      statusLock = statusLock,
      titleLock = titleLock,
      titleSortLock = titleSortLock,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone()
    )
}
