package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.jooq.Sequences
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.SeriesRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDateTime

@Component
class SeriesDao(
  private val dsl: DSLContext
) : SeriesRepository {

  private val s = Tables.SERIES
  private val d = Tables.SERIES_METADATA
  private val b = Tables.BOOK


  override fun findAll(): Collection<Series> =
    dsl.selectFrom(s)
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByIdOrNull(seriesId: Long): Series? =
    dsl.selectFrom(s)
      .where(s.ID.eq(seriesId))
      .fetchOneInto(s)
      ?.toDomain()

  override fun findByLibraryId(libraryId: Long): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByLibraryIdAndUrlNotIn(libraryId: Long, urls: Collection<URL>): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.notIn(urls.map { it.toString() })))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByLibraryIdAndUrl(libraryId: Long, url: URL): Series? =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.eq(url.toString())))
      .fetchOneInto(s)
      ?.toDomain()


  override fun getLibraryId(seriesId: Long): Long? =
    dsl.select(s.LIBRARY_ID)
      .from(s)
      .where(s.ID.eq(seriesId))
      .fetchOne(0, Long::class.java)


  override fun findAll(search: SeriesSearch): Collection<Series> {
    val conditions = search.toCondition()

    return dsl.selectFrom(s)
      .where(conditions)
      .fetchInto(s)
      .map { it.toDomain() }
  }


  override fun insert(series: Series): Series {
    val id = dsl.nextval(Sequences.HIBERNATE_SEQUENCE)

    dsl.insertInto(s)
      .set(s.ID, id)
      .set(s.NAME, series.name)
      .set(s.URL, series.url.toString())
      .set(s.FILE_LAST_MODIFIED, series.fileLastModified)
      .set(s.LIBRARY_ID, series.libraryId)
      .execute()

    return findByIdOrNull(id)!!
  }

  override fun update(series: Series) {
    dsl.update(s)
      .set(s.NAME, series.name)
      .set(s.URL, series.url.toString())
      .set(s.FILE_LAST_MODIFIED, series.fileLastModified)
      .set(s.LIBRARY_ID, series.libraryId)
      .set(s.LAST_MODIFIED_DATE, LocalDateTime.now())
      .where(s.ID.eq(series.id))
      .execute()
  }

  override fun delete(seriesId: Long) {
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

  override fun deleteAll(seriesIds: Collection<Long>) {
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

    if (libraryIds.isNotEmpty()) c = c.and(s.LIBRARY_ID.`in`(libraryIds))
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(searchTerm)) }
    if (metadataStatus.isNotEmpty()) c = c.and(d.STATUS.`in`(metadataStatus))

    return c
  }

  private fun SeriesRecord.toDomain() =
    Series(
      name = name,
      url = URL(url),
      fileLastModified = fileLastModified,
      id = id,
      libraryId = libraryId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )
}
