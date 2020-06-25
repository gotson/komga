package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.gotson.komga.interfaces.rest.dto.SeriesMetadataDto
import org.gotson.komga.interfaces.rest.dto.toUTC
import org.gotson.komga.interfaces.rest.persistence.SeriesDtoRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.SeriesMetadataRecord
import org.gotson.komga.jooq.tables.records.SeriesRecord
import org.jooq.AggregateFunction
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.SelectOnConditionStep
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.net.URL

const val BOOKS_COUNT = "booksCount"
const val BOOKS_UNREAD_COUNT = "booksUnreadCount"
const val BOOKS_IN_PROGRESS_COUNT = "booksInProgressCount"
const val BOOKS_READ_COUNT = "booksReadCount"

@Component
class SeriesDtoDao(
  private val dsl: DSLContext
) : SeriesDtoRepository {

  companion object {
    private val s = Tables.SERIES
    private val b = Tables.BOOK
    private val d = Tables.SERIES_METADATA
    private val r = Tables.READ_PROGRESS
    private val cs = Tables.COLLECTION_SERIES

    val countUnread: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isNull, 1).otherwise(0))
    val countRead: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isTrue, 1).otherwise(0))
    val countInProgress: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isFalse, 1).otherwise(0))
  }

  private val groupFields = arrayOf(
    *s.fields(),
    *d.fields()
  )

  private val sorts = mapOf(
    "metadata.titleSort" to DSL.lower(d.TITLE_SORT),
    "createdDate" to s.CREATED_DATE,
    "created" to s.CREATED_DATE,
    "lastModifiedDate" to s.LAST_MODIFIED_DATE,
    "lastModified" to s.LAST_MODIFIED_DATE
  )

  override fun findAll(search: SeriesSearchWithReadProgress, userId: Long, pageable: Pageable): Page<SeriesDto> {
    val conditions = search.toCondition()

    val having = search.readStatus?.toCondition() ?: DSL.trueCondition()

    return findAll(conditions, having, userId, pageable)
  }

  override fun findRecentlyUpdated(search: SeriesSearchWithReadProgress, userId: Long, pageable: Pageable): Page<SeriesDto> {
    val conditions = search.toCondition()
      .and(s.CREATED_DATE.ne(s.LAST_MODIFIED_DATE))

    val having = search.readStatus?.toCondition() ?: DSL.trueCondition()

    return findAll(conditions, having, userId, pageable)
  }

  override fun findByIdOrNull(seriesId: Long, userId: Long): SeriesDto? =
    selectBase(userId)
      .where(s.ID.eq(seriesId))
      .groupBy(*groupFields)
      .fetchAndMap()
      .firstOrNull()

  override fun findByIds(seriesIds: Collection<Long>, userId: Long): List<SeriesDto> =
    selectBase(userId)
      .where(s.ID.`in`(seriesIds))
      .groupBy(*groupFields)
      .fetchAndMap()


  private fun findAll(conditions: Condition, having: Condition, userId: Long, pageable: Pageable): Page<SeriesDto> {
    val count = dsl.select(s.ID)
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
      .leftJoin(cs).on(s.ID.eq(cs.SERIES_ID))
      .where(conditions)
      .groupBy(s.ID)
      .having(having)
      .fetch()
      .size

    val orderBy = pageable.sort.toOrderBy(sorts)

    val dtos = selectBase(userId)
      .where(conditions)
      .groupBy(*groupFields)
      .having(having)
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

  private fun selectBase(userId: Long): SelectOnConditionStep<Record> =
    dsl.select(*groupFields)
      .select(DSL.count(b.ID).`as`(BOOKS_COUNT))
      .select(countUnread.`as`(BOOKS_UNREAD_COUNT))
      .select(countRead.`as`(BOOKS_READ_COUNT))
      .select(countInProgress.`as`(BOOKS_IN_PROGRESS_COUNT))
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
      .leftJoin(cs).on(s.ID.eq(cs.SERIES_ID))
      .and(readProgressCondition(userId))

  private fun readProgressCondition(userId: Long): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)

  private fun ResultQuery<Record>.fetchAndMap() =
    fetch()
      .map { r ->
        val sr = r.into(s)
        val dr = r.into(d)
        val booksCount = r.get(BOOKS_COUNT, Int::class.java)
        val booksUnreadCount = r.get(BOOKS_UNREAD_COUNT, Int::class.java)
        val booksReadCount = r.get(BOOKS_READ_COUNT, Int::class.java)
        val booksInProgressCount = r.get(BOOKS_IN_PROGRESS_COUNT, Int::class.java)
        sr.toDto(booksCount, booksReadCount, booksUnreadCount, booksInProgressCount, dr.toDto())
      }

  private fun SeriesSearchWithReadProgress.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    libraryIds?.let { c = c.and(s.LIBRARY_ID.`in`(it)) }
    collectionIds?.let { c = c.and(cs.COLLECTION_ID.`in`(it)) }
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(it)) }
    metadataStatus?.let { c = c.and(d.STATUS.`in`(it)) }

    return c
  }

  private fun Collection<ReadStatus>.toCondition(): Condition =
    map {
      when (it) {
        ReadStatus.UNREAD -> countUnread.ge(1.toBigDecimal())
        ReadStatus.READ -> countRead.ge(1.toBigDecimal())
        ReadStatus.IN_PROGRESS -> countInProgress.ge(1.toBigDecimal())
      }
    }.reduce { acc, condition -> acc.or(condition) }

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


