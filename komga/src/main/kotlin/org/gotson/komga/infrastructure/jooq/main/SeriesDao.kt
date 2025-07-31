package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.jooq.RequiredJoin
import org.gotson.komga.infrastructure.jooq.SeriesSearchHelper
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.infrastructure.jooq.csAlias
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.SeriesRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.unsorted
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SeriesDao(
  private val dslRW: DSLContext,
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SeriesRepository {
  private val s = Tables.SERIES
  private val d = Tables.SERIES_METADATA
  private val rs = Tables.READ_PROGRESS_SERIES
  private val bma = Tables.BOOK_METADATA_AGGREGATION

  override fun findAll(): Collection<Series> =
    dslRO
      .selectFrom(s)
      .fetchInto(s)
      .map { it.toDomain() }

  override fun findByIdOrNull(seriesId: String): Series? =
    dslRO
      .selectFrom(s)
      .where(s.ID.eq(seriesId))
      .fetchOneInto(s)
      ?.toDomain()

  override fun findAllByLibraryId(libraryId: String): List<Series> =
    dslRO
      .selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId))
      .fetchInto(s)
      .map { it.toDomain() }

  @Transactional
  override fun findAllNotDeletedByLibraryIdAndUrlNotIn(
    libraryId: String,
    urls: Collection<URL>,
  ): List<Series> {
    dslRO.withTempTable(batchSize, urls.map { it.toString() }).use { tempTable ->
      return dslRO
        .selectFrom(s)
        .where(s.LIBRARY_ID.eq(libraryId))
        .and(s.DELETED_DATE.isNull)
        .and(s.URL.notIn(tempTable.selectTempStrings()))
        .fetchInto(s)
        .map { it.toDomain() }
    }
  }

  override fun findNotDeletedByLibraryIdAndUrlOrNull(
    libraryId: String,
    url: URL,
  ): Series? =
    dslRO
      .selectFrom(s)
      .where(s.LIBRARY_ID.eq(libraryId).and(s.URL.eq(url.toString())))
      .and(s.DELETED_DATE.isNull)
      .orderBy(s.LAST_MODIFIED_DATE.desc())
      .fetchInto(s)
      .firstOrNull()
      ?.toDomain()

  override fun findAllByTitleContaining(title: String): Collection<Series> =
    dslRO
      .selectDistinct(*s.fields())
      .from(s)
      .leftJoin(d)
      .on(s.ID.eq(d.SERIES_ID))
      .where(d.TITLE.containsIgnoreCase(title))
      .fetchInto(s)
      .map { it.toDomain() }

  override fun getLibraryId(seriesId: String): String? =
    dslRO
      .select(s.LIBRARY_ID)
      .from(s)
      .where(s.ID.eq(seriesId))
      .fetchOne(0, String::class.java)

  override fun findAllIdsByLibraryId(libraryId: String): Collection<String> =
    dslRO
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

    val query =
      dslRO
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

    val count = dslRO.fetchCount(query)

    val items =
      query
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(s)
        .map { it.toDomain() }

    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, unsorted())
      else
        PageRequest.of(0, maxOf(count, 20), unsorted()),
      count.toLong(),
    )
  }

  override fun insert(series: Series) {
    dslRW
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
    dslRW
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
    dslRW.deleteFrom(s).where(s.ID.eq(seriesId)).execute()
  }

  override fun deleteAll() {
    dslRW.deleteFrom(s).execute()
  }

  @Transactional
  override fun delete(seriesIds: Collection<String>) {
    dslRW.withTempTable(batchSize, seriesIds).use {
      dslRW.deleteFrom(s).where(s.ID.`in`(it.selectTempStrings())).execute()
    }
  }

  override fun count(): Long = dslRO.fetchCount(s).toLong()

  override fun countGroupedByLibraryId(): Map<String, Int> =
    dslRO
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
