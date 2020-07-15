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


  override fun findAll(): Collection<Series> =
    dsl.selectFrom(s)
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByIdOrNull(seriesId: String): Series? =
    dsl.selectFrom(s)
      .where(s.ID.eq(seriesId))
      .fetchOneInto(s)
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByLibraryIdAndUrlNotIn(libraryId: String, urls: Collection<URL>): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.notIn(urls.map { it.toString() })))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByLibraryIdAndUrl(libraryId: String, url: URL): Series? =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.eq(url.toString())))
      .fetchOneInto(s)
      ?.toDomain()


  override fun getLibraryId(seriesId: String): String? =
    dsl.select(s.LIBRARY_ID)
      .from(s)
      .where(s.ID.eq(seriesId))
      .fetchOne(0, String::class.java)


  override fun findAll(search: SeriesSearch): Collection<Series> {
    val conditions = search.toCondition()

    return dsl.select(*s.fields())
      .from(s)
      .leftJoin(cs).on(s.ID.eq(cs.SERIES_ID))
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .where(conditions)
      .fetchInto(s)
      .map { it.toDomain() }
  }


  override fun insert(series: Series) {
    dsl.insertInto(s)
      .set(s.ID, series.id)
      .set(s.NAME, series.name)
      .set(s.URL, series.url.toString())
      .set(s.FILE_LAST_MODIFIED, series.fileLastModified)
      .set(s.LIBRARY_ID, series.libraryId)
      .execute()
  }

  override fun update(series: Series) {
    dsl.update(s)
      .set(s.NAME, series.name)
      .set(s.URL, series.url.toString())
      .set(s.FILE_LAST_MODIFIED, series.fileLastModified)
      .set(s.LIBRARY_ID, series.libraryId)
      .set(s.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(s.ID.eq(series.id))
      .execute()
  }

  override fun delete(seriesId: String) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(d).where(d.SERIES_ID.eq(seriesId)).execute()
        deleteFrom(s).where(s.ID.eq(seriesId)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(d).execute()
        deleteFrom(s).execute()
      }
    }
  }

  override fun deleteAll(seriesIds: Collection<String>) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(d).where(d.SERIES_ID.`in`(seriesIds)).execute()
        deleteFrom(s).where(s.ID.`in`(seriesIds)).execute()
      }
    }
  }

  override fun count(): Long = dsl.fetchCount(s).toLong()


  private fun SeriesSearch.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    libraryIds?.let { c = c.and(s.LIBRARY_ID.`in`(it)) }
    collectionIds?.let { c = c.and(cs.COLLECTION_ID.`in`(it)) }
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(it)) }
    metadataStatus?.let { c = c.and(d.STATUS.`in`(it)) }

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
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone()
    )
}
