package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.SeriesRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SeriesDao(
  private val dsl: DSLContext,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SeriesRepository {
  private val s = Tables.SERIES
  private val d = Tables.SERIES_METADATA
  private val cs = Tables.COLLECTION_SERIES
  private val l = Tables.LIBRARY

  override fun findAll(): Collection<Series> =
    dsl.selectFrom(s)
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByIdOrNull(seriesId: String): Series? =
    dsl.selectFrom(s)
      .where(s.ID.eq(seriesId))
      .fetchOneInto(s)
      ?.toDomain()

  override fun findAllByLibraryId(libraryId: String): List<Series> =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetchInto(s)
      .map { it.toDomain() }

  @Transactional
  override fun findAllNotDeletedByLibraryIdAndUrlNotIn(
    libraryId: String,
    urls: Collection<URL>,
  ): List<Series> {
    dsl.insertTempStrings(batchSize, urls.map { it.toString() })

    return dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .and(s.DELETED_DATE.isNull)
      .and(s.URL.notIn(dsl.selectTempStrings()))
      .fetchInto(s)
      .map { it.toDomain() }
  }

  override fun findNotDeletedByLibraryIdAndUrlOrNull(
    libraryId: String,
    url: URL,
  ): Series? =
    dsl.selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.eq(url.toString())))
      .and(s.DELETED_DATE.isNull)
      .orderBy(s.LAST_MODIFIED_DATE.desc())
      .fetchInto(s)
      .firstOrNull()
      ?.toDomain()

  override fun findAllByTitleContaining(title: String): Collection<Series> =
    dsl.selectDistinct(*s.fields())
      .from(s)
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .where(d.TITLE.containsIgnoreCase(title))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun getLibraryId(seriesId: String): String? =
    dsl.select(s.LIBRARY_ID)
      .from(s)
      .where(s.ID.eq(seriesId))
      .fetchOne(0, String::class.java)

  override fun findAllIdsByLibraryId(libraryId: String): Collection<String> =
    dsl.select(s.ID)
      .from(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetch(s.ID)

  override fun findAll(search: SeriesSearch): Collection<Series> {
    val conditions = search.toCondition()

    return dsl.selectDistinct(*s.fields())
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
      .set(s.DELETED_DATE, series.deletedDate)
      .set(s.ONESHOT, series.oneshot)
      .execute()
  }

  override fun update(
    series: Series,
    updateModifiedTime: Boolean,
  ) {
    dsl.update(s)
      .set(s.NAME, series.name)
      .set(s.URL, series.url.toString())
      .set(s.FILE_LAST_MODIFIED, series.fileLastModified)
      .set(s.LIBRARY_ID, series.libraryId)
      .set(s.BOOK_COUNT, series.bookCount)
      .set(s.DELETED_DATE, series.deletedDate)
      .set(s.ONESHOT, series.oneshot)
      .apply { if (updateModifiedTime) set(s.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z"))) }
      .where(s.ID.eq(series.id))
      .execute()
  }

  override fun delete(seriesId: String) {
    dsl.deleteFrom(s).where(s.ID.eq(seriesId)).execute()
  }

  override fun deleteAll() {
    dsl.deleteFrom(s).execute()
  }

  @Transactional
  override fun delete(seriesIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, seriesIds)

    dsl.deleteFrom(s).where(s.ID.`in`(dsl.selectTempStrings())).execute()
  }

  override fun count(): Long = dsl.fetchCount(s).toLong()

  override fun countGroupedByLibraryId(): Map<String, Int> =
    dsl.select(s.LIBRARY_ID, DSL.count(s.ID))
      .from(s)
      .groupBy(s.LIBRARY_ID)
      .fetchMap(s.LIBRARY_ID, DSL.count(s.ID))

  private fun SeriesSearch.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    if (libraryIds != null) c = c.and(s.LIBRARY_ID.`in`(libraryIds))
    if (!collectionIds.isNullOrEmpty()) c = c.and(cs.COLLECTION_ID.`in`(collectionIds))
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(it)) }
    searchRegex?.let { c = c.and((it.second.toColumn()).likeRegex(it.first)) }
    if (!metadataStatus.isNullOrEmpty()) c = c.and(d.STATUS.`in`(metadataStatus))
    if (!publishers.isNullOrEmpty()) c = c.and(d.PUBLISHER.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(publishers))
    if (deleted == true) c = c.and(s.DELETED_DATE.isNotNull)
    if (deleted == false) c = c.and(s.DELETED_DATE.isNull)

    return c
  }

  private fun SeriesSearch.SearchField.toColumn() =
    when (this) {
      SeriesSearch.SearchField.NAME -> s.NAME
      SeriesSearch.SearchField.TITLE -> d.TITLE
      SeriesSearch.SearchField.TITLE_SORT -> d.TITLE_SORT
    }

  private fun SeriesRecord.toDomain() =
    Series(
      name = name,
      url = URL(url),
      fileLastModified = fileLastModified,
      id = id,
      libraryId = libraryId,
      bookCount = bookCount,
      deletedDate = deletedDate,
      oneshot = oneshot,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
    )
}
