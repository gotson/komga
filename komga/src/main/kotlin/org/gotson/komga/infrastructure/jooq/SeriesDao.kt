package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.SeriesRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SeriesDao(
  private val dsl: DSLContext
) : SeriesRepository {

  private val s = Tables.SERIES
  private val d = Tables.SERIES_METADATA
  private val cs = Tables.COLLECTION_SERIES
  private val b = Tables.BOOK

  override fun findAll(): Collection<Series> =
    dsl.selectFrom(s)
      .where(s.DELETED.eq(false))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByIdOrNull(seriesId: String): Series? =
    dsl.selectFrom(s)
      .where(s.ID.eq(seriesId))
      .and(s.DELETED.eq(false))
      .fetchOneInto(s)
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .and(s.DELETED.eq(false))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByLibraryIdIncludeDeleted(libraryId: String): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByLibraryIdAndUrlNotIn(libraryId: String, urls: Collection<URL>): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.notIn(urls.map { it.toString() })))
      .and(s.DELETED.eq(false))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun existsByLibraryIdAndUrl(libraryId: String, url: URL): Boolean =
    dsl.fetchExists(
      dsl.selectOne()
        .from(s)
        .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.eq(url.toString())))
        .and(s.DELETED.eq(false))
    )

  override fun findByLibraryIdAndUrlIncludeDeleted(libraryId: String, url: URL): Series? =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.eq(url.toString())))
      .fetchOneInto(s)
      ?.toDomain()

  override fun findByHashesInIncludeDeleted(hashes: Collection<String>): List<Series> =
    dsl.select(*s.fields())
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .where(b.FILE_HASH.`in`(hashes))
      .groupBy(s.ID)
      .having(DSL.count().eq(hashes.size))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findAllDeleted(): Collection<String> =
    dsl.select(s.ID)
      .from(s)
      .where(s.DELETED.eq(true))
      .fetch(0, String::class.java)

  override fun findAll(search: SeriesSearch): Collection<Series> {
    val conditions = search.toCondition()

    return dsl.selectDistinct(*s.fields())
      .from(s)
      .leftJoin(cs).on(s.ID.eq(cs.SERIES_ID))
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .where(conditions)
      .and(s.DELETED.eq(false))
      .fetchInto(s)
      .map { it.toDomain() }
  }

  override fun getLibraryId(seriesId: String): String? =
    dsl.select(s.LIBRARY_ID)
      .from(s)
      .where(s.ID.eq(seriesId))
      .and(s.DELETED.eq(false))
      .fetchOne(0, String::class.java)

  override fun insert(series: Series) {
    dsl.insertInto(s)
      .set(s.ID, series.id)
      .set(s.NAME, series.name)
      .set(s.URL, series.url.toString())
      .set(s.FILE_LAST_MODIFIED, series.fileLastModified)
      .set(s.LIBRARY_ID, series.libraryId)
      .set(s.DELETED, series.deleted)
      .execute()
  }

  override fun update(series: Series) {
    dsl.update(s)
      .set(s.NAME, series.name)
      .set(s.URL, series.url.toString())
      .set(s.FILE_LAST_MODIFIED, series.fileLastModified)
      .set(s.LIBRARY_ID, series.libraryId)
      .set(s.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .set(s.DELETED, series.deleted)
      .where(s.ID.eq(series.id))
      .execute()
  }

  override fun delete(seriesId: String) {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(s).where(s.ID.eq(seriesId)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(s).execute()
      }
    }
  }

  override fun deleteAll(seriesIds: Collection<String>) {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(s).where(s.ID.`in`(seriesIds)).execute()
      }
    }
  }

  override fun softDeleteAll(seriesIds: Collection<String>) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        update(s).set(s.DELETED, true).where(s.ID.`in`(seriesIds)).execute()
      }
    }
  }

  override fun count(): Long = dsl.selectCount()
    .from(s)
    .where(s.DELETED.eq(false))
    .fetchOne(0, Long::class.java)

  private fun SeriesSearch.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    if (!libraryIds.isNullOrEmpty()) c = c.and(s.LIBRARY_ID.`in`(libraryIds))
    if (!collectionIds.isNullOrEmpty()) c = c.and(cs.COLLECTION_ID.`in`(collectionIds))
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(it)) }
    if (!metadataStatus.isNullOrEmpty()) c = c.and(d.STATUS.`in`(metadataStatus))
    if (!publishers.isNullOrEmpty()) c = c.and(DSL.lower(d.PUBLISHER).`in`(publishers.map { it.toLowerCase() }))

    return c
  }

  private fun SeriesRecord.toDomain() =
    Series(
      name = name,
      url = URL(url),
      fileLastModified = fileLastModified,
      id = id,
      libraryId = libraryId,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      deleted = deleted
    )
}
