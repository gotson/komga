package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.gotson.komga.interfaces.rest.dto.SeriesMetadataDto
import org.gotson.komga.interfaces.rest.dto.toUTC
import org.gotson.komga.interfaces.rest.persistence.SeriesDtoRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.SeriesMetadataRecord
import org.gotson.komga.jooq.tables.records.SeriesRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.net.URL

@Component
class SeriesDtoDao(
  private val dsl: DSLContext
) : SeriesDtoRepository {

  private val s = Tables.SERIES
  private val b = Tables.BOOK
  private val d = Tables.SERIES_METADATA
  private val r = Tables.READ_PROGRESS

  private val groupFields = arrayOf(
    *s.fields(),
    *d.fields()
  )

  private val sorts = mapOf(
    "metadata.titleSort" to DSL.lower(d.TITLE_SORT),
    "createdDate" to s.CREATED_DATE,
    "lastModifiedDate" to s.LAST_MODIFIED_DATE
  )

  override fun findAll(search: SeriesSearch, userId: Long, pageable: Pageable): Page<SeriesDto> {
    val conditions = search.toCondition()

    return findAll(conditions, userId, pageable)
  }

  override fun findRecentlyUpdated(search: SeriesSearch, userId: Long, pageable: Pageable): Page<SeriesDto> {
    val conditions = search.toCondition()
      .and(s.CREATED_DATE.ne(s.LAST_MODIFIED_DATE))

    return findAll(conditions, userId, pageable)
  }

  override fun findByIdOrNull(seriesId: Long, userId: Long): SeriesDto? =
    selectBase()
      .where(s.ID.eq(seriesId))
      .and(readProgressCondition(userId))
      .groupBy(*groupFields)
      .fetchAndMap()
      .firstOrNull()


  private fun findAll(conditions: Condition, userId: Long, pageable: Pageable): Page<SeriesDto> {
    val count = dsl.selectCount()
      .from(s)
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .where(conditions)
      .fetchOne(0, Int::class.java)

    val orderBy = pageable.sort.toOrderBy(sorts)

    val dtos = selectBase()
      .where(conditions)
      .and(readProgressCondition(userId))
      .groupBy(*groupFields)
      .orderBy(orderBy)
      .limit(pageable.pageSize)
      .offset(pageable.offset)
      .fetchAndMap()

    return PageImpl(
      dtos,
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageable.sort),
      count.toLong()
    )
  }

  private fun selectBase() =
    dsl.select(*groupFields)
      .select(DSL.count(b.ID).`as`("booksCount"))
      .select(DSL.sum(DSL.`when`(r.COMPLETED.isTrue, 1).otherwise(0)).`as`("booksReadCount"))
      .select(DSL.sum(DSL.`when`(r.COMPLETED.isFalse, 1).otherwise(0)).`as`("booksInProgressCount"))
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))

  private fun readProgressCondition(userId: Long): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)

  private fun ResultQuery<Record>.fetchAndMap() =
    fetch()
      .map { r ->
        val sr = r.into(s)
        val dr = r.into(d)
        val booksCount = r.get("booksCount", Int::class.java)
        val booksReadCount = r.get("booksReadCount", Int::class.java)
        val booksInProgressCount = r.get("booksInProgressCount", Int::class.java)
        val booksUnreadCount = booksCount - booksInProgressCount - booksReadCount
        sr.toDto(booksCount, booksReadCount, booksUnreadCount, booksInProgressCount, dr.toDto())
      }

  private fun SeriesSearch.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    if (libraryIds.isNotEmpty()) c = c.and(s.LIBRARY_ID.`in`(libraryIds))
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(searchTerm)) }
    if (metadataStatus.isNotEmpty()) c = c.and(d.STATUS.`in`(metadataStatus))

    return c
  }

  private fun SeriesRecord.toDto(booksCount: Int, booksReadCount: Int, booksUnreadCount: Int, booksInProgressCount: Int, metadata: SeriesMetadataDto) =
    SeriesDto(
      id = id,
      libraryId = libraryId,
      name = name,
      url = URL(url).toURI().path,
      created = createdDate.toUTC(),
      lastModified = lastModifiedDate.toUTC(),
      fileLastModified = fileLastModified.toUTC(),
      booksCount = booksCount,
      booksReadCount = booksReadCount,
      booksUnreadCount = booksUnreadCount,
      booksInProgressCount = booksInProgressCount,
      metadata = metadata
    )

  private fun SeriesMetadataRecord.toDto() =
    SeriesMetadataDto(
      status = status,
      statusLock = statusLock,
      created = createdDate.toUTC(),
      lastModified = lastModifiedDate.toUTC(),
      title = title,
      titleLock = titleLock,
      titleSort = titleSort,
      titleSortLock = titleSortLock
    )
}


