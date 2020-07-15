package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.gotson.komga.interfaces.rest.dto.SeriesMetadataDto
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
import org.jooq.impl.DSL.inline
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import toFilePath
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
    "lastModified" to s.LAST_MODIFIED_DATE,
    "collection.number" to cs.NUMBER
  )

  override fun findAll(search: SeriesSearchWithReadProgress, userId: String, pageable: Pageable): Page<SeriesDto> {
    val conditions = search.toCondition()

    val having = search.readStatus?.toCondition() ?: DSL.trueCondition()

    return findAll(conditions, having, userId, pageable)
  }

  override fun findByCollectionId(collectionId: String, userId: String, pageable: Pageable): Page<SeriesDto> {
    val conditions = cs.COLLECTION_ID.eq(collectionId)
    val having = DSL.trueCondition()

    return findAll(conditions, having, userId, pageable, true)
  }

  override fun findRecentlyUpdated(search: SeriesSearchWithReadProgress, userId: String, pageable: Pageable): Page<SeriesDto> {
    val conditions = search.toCondition()
      .and(s.CREATED_DATE.ne(s.LAST_MODIFIED_DATE))

    val having = search.readStatus?.toCondition() ?: DSL.trueCondition()

    return findAll(conditions, having, userId, pageable)
  }

  override fun findByIdOrNull(seriesId: String, userId: String): SeriesDto? =
    selectBase(userId)
      .where(s.ID.eq(seriesId))
      .groupBy(*groupFields)
      .fetchAndMap(userId)
      .firstOrNull()


  private fun selectBase(userId: String, selectCollectionNumber: Boolean = false): SelectOnConditionStep<Record> =
    dsl.selectDistinct(*groupFields)
      .select(DSL.countDistinct(b.ID).`as`(BOOKS_COUNT))
      .apply { if (selectCollectionNumber) select(cs.NUMBER) }
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
      .and(readProgressCondition(userId))
      .leftJoin(cs).on(s.ID.eq(cs.SERIES_ID))

  private fun findAll(conditions: Condition, having: Condition, userId: String, pageable: Pageable, selectCollectionNumber: Boolean = false): Page<SeriesDto> {
    val count = dsl.selectDistinct(s.ID)
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
      .and(readProgressCondition(userId))
      .leftJoin(cs).on(s.ID.eq(cs.SERIES_ID))
      .where(conditions)
      .groupBy(s.ID)
      .having(having)
      .fetch()
      .size

    val orderBy = pageable.sort.toOrderBy(sorts)

    val dtos = selectBase(userId, selectCollectionNumber)
      .where(conditions)
      .groupBy(*groupFields)
      .having(having)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchAndMap(userId)

    val pageSort = if (orderBy.size > 1) pageable.sort else Sort.unsorted()
    return PageImpl(
      dtos,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong()
    )
  }

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)

  private fun ResultQuery<Record>.fetchAndMap(userId: String) =
    fetch()
      .map { rec ->
        val sr = rec.into(s)
        val dr = rec.into(d)
        val booksCount = rec.get(BOOKS_COUNT, Int::class.java)

        val booksCountRecord = dsl
          .select(countUnread.`as`(BOOKS_UNREAD_COUNT))
          .select(countRead.`as`(BOOKS_READ_COUNT))
          .select(countInProgress.`as`(BOOKS_IN_PROGRESS_COUNT))
          .from(b)
          .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
          .and(readProgressCondition(userId))
          .where(b.SERIES_ID.eq(sr.id))
          .fetch()
          .first()

        val booksUnreadCount = booksCountRecord.get(BOOKS_UNREAD_COUNT, Int::class.java)
        val booksReadCount = booksCountRecord.get(BOOKS_READ_COUNT, Int::class.java)
        val booksInProgressCount = booksCountRecord.get(BOOKS_IN_PROGRESS_COUNT, Int::class.java)
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
        ReadStatus.UNREAD -> countUnread.ge(inline(1.toBigDecimal()))
        ReadStatus.READ -> countRead.ge(inline(1.toBigDecimal()))
        ReadStatus.IN_PROGRESS -> countInProgress.ge(inline(1.toBigDecimal()))
      }
    }.reduce { acc, condition -> acc.or(condition) }

  private fun SeriesRecord.toDto(booksCount: Int, booksReadCount: Int, booksUnreadCount: Int, booksInProgressCount: Int, metadata: SeriesMetadataDto) =
    SeriesDto(
      id = id,
      libraryId = libraryId,
      name = name,
      url = URL(url).toFilePath(),
      created = createdDate,
      lastModified = lastModifiedDate,
      fileLastModified = fileLastModified,
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
      created = createdDate,
      lastModified = lastModifiedDate,
      title = title,
      titleLock = titleLock,
      titleSort = titleSort,
      titleSortLock = titleSortLock
    )
}


