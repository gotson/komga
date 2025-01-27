package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jooq.RequiredJoin
import org.gotson.komga.infrastructure.jooq.SeriesSearchHelper
import org.gotson.komga.infrastructure.jooq.csAlias
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.SeriesRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
  private val rs = Tables.READ_PROGRESS_SERIES
  private val bma = Tables.BOOK_METADATA_AGGREGATION

  override fun findAll(): Collection<Series> =
    dsl
      .selectFrom(s)
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByIdOrNull(seriesId: String): Series? =
    dsl
      .selectFrom(s)
      .where(s.ID.eq(seriesId))
      .fetchOneInto(s)
      ?.toDomain()

  override fun findAllByLibraryId(libraryId: String): List<Series> =
    dsl
      .selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetchInto(s)
      .map { it.toDomain() }

  @Transactional
  override fun findAllNotDeletedByLibraryIdAndUrlNotIn(
    libraryId: String,
    urls: Collection<URL>,
  ): List<Series> {
    dsl.insertTempStrings(batchSize, urls.map { it.toString() })

    return dsl
      .selectFrom(s)
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
    dsl
      .selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.eq(url.toString())))
      .and(s.DELETED_DATE.isNull)
      .orderBy(s.LAST_MODIFIED_DATE.desc())
      .fetchInto(s)
      .firstOrNull()
      ?.toDomain()

  override fun findAllByTitleContaining(title: String): Collection<Series> =
    dsl
      .selectDistinct(*s.fields())
      .from(s)
      .leftJoin(d)
      .on(s.ID.eq(d.SERIES_ID))
      .where(d.TITLE.containsIgnoreCase(title))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun getLibraryId(seriesId: String): String? =
    dsl
      .select(s.LIBRARY_ID)
      .from(s)
      .where(s.ID.eq(seriesId))
      .fetchOne(0, String::class.java)

  override fun findAllIdsByLibraryId(libraryId: String): Collection<String> =
    dsl
      .select(s.ID)
      .from(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetch(s.ID)

  override fun findAll(
    searchCondition: SearchCondition.Series?,
    searchContext: SearchContext,
    pageable: Pageable,
  ): Page<Series> {
    val (conditions, joins) = SeriesSearchHelper(searchContext).toCondition(searchCondition)
    return findAll(conditions, joins, pageable)
  }

  private fun findAll(
    conditions: Condition,
    joins: Set<RequiredJoin>,
    pageable: Pageable,
  ): Page<Series> {
    val query =
      dsl
        .selectDistinct(*s.fields())
        .from(s)
        .apply {
          joins.forEach { join ->
            when (join) {
              is RequiredJoin.Collection -> {
                val csAlias = csAlias(join.collectionId)
                leftJoin(csAlias).on(s.ID.eq(csAlias.SERIES_ID).and(csAlias.COLLECTION_ID.eq(join.collectionId)))
              }
              RequiredJoin.BookMetadataAggregation -> leftJoin(bma).on(s.ID.eq(bma.SERIES_ID))
              RequiredJoin.SeriesMetadata -> innerJoin(d).on(s.ID.eq(d.SERIES_ID))
              is RequiredJoin.ReadProgress -> leftJoin(rs).on(rs.SERIES_ID.eq(s.ID)).and(rs.USER_ID.eq(join.userId))
              // Book joins - not needed
              RequiredJoin.BookMetadata -> Unit
              RequiredJoin.Media -> Unit
              is RequiredJoin.ReadList -> Unit
            }
          }
        }.where(conditions)

    val count = dsl.fetchCount(query)
    val items =
      query
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(s)
        .map { it.toDomain() }

    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.unsorted())
      else
        PageRequest.of(0, maxOf(count, 20), Sort.unsorted()),
      count.toLong(),
    )
  }

  override fun insert(series: Series) {
    dsl
      .insertInto(s)
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
    dsl
      .update(s)
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
    dsl
      .select(s.LIBRARY_ID, DSL.count(s.ID))
      .from(s)
      .groupBy(s.LIBRARY_ID)
      .fetchMap(s.LIBRARY_ID, DSL.count(s.ID))

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
