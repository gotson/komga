package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.interfaces.api.persistence.ReadProgressDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.TachiyomiReadProgressDto
import org.gotson.komga.interfaces.api.rest.dto.TachiyomiReadProgressV2Dto
import org.gotson.komga.jooq.main.Tables
import org.jooq.AggregateFunction
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record2
import org.jooq.impl.DSL
import org.jooq.impl.DSL.rowNumber
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ReadProgressDtoDao(
  private val dsl: DSLContext,
) : ReadProgressDtoRepository {
  private val rlb = Tables.READLIST_BOOK
  private val b = Tables.BOOK
  private val d = Tables.BOOK_METADATA
  private val r = Tables.READ_PROGRESS

  private val countUnread: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isNull, 1).otherwise(0))
  private val countRead: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isTrue, 1).otherwise(0))
  private val countInProgress: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isFalse, 1).otherwise(0))

  override fun findProgressV2BySeries(
    seriesId: String,
    userId: String,
  ): TachiyomiReadProgressV2Dto {
    val numberSortReadProgress =
      dsl
        .select(
          d.NUMBER_SORT,
          r.COMPLETED,
        ).from(b)
        .leftJoin(r)
        .on(b.ID.eq(r.BOOK_ID))
        .and(readProgressCondition(userId))
        .leftJoin(d)
        .on(b.ID.eq(d.BOOK_ID))
        .where(b.SERIES_ID.eq(seriesId))
        .orderBy(d.NUMBER_SORT)
        .fetch()
        .toList()

    val maxNumberSort =
      dsl
        .select(DSL.max(d.NUMBER_SORT))
        .from(b)
        .leftJoin(d)
        .on(b.ID.eq(d.BOOK_ID))
        .where(b.SERIES_ID.eq(seriesId))
        .fetchOne(DSL.max(d.NUMBER_SORT)) ?: 0F

    val booksCount = getSeriesBooksCount(seriesId, userId)

    return booksCountToDtoV2(booksCount, numberSortReadProgress.lastRead() ?: 0F, maxNumberSort)
  }

  private fun getSeriesBooksCount(
    seriesId: String,
    userId: String,
  ) = dsl
    .select(countUnread.`as`(BOOKS_UNREAD_COUNT))
    .select(countRead.`as`(BOOKS_READ_COUNT))
    .select(countInProgress.`as`(BOOKS_IN_PROGRESS_COUNT))
    .from(b)
    .leftJoin(r)
    .on(b.ID.eq(r.BOOK_ID))
    .and(readProgressCondition(userId))
    .where(b.SERIES_ID.eq(seriesId))
    .fetch()
    .first()
    .map {
      BooksCount(
        unreadCount = it.get(BOOKS_UNREAD_COUNT, Int::class.java),
        readCount = it.get(BOOKS_READ_COUNT, Int::class.java),
        inProgressCount = it.get(BOOKS_IN_PROGRESS_COUNT, Int::class.java),
      )
    }

  override fun findProgressByReadList(
    readListId: String,
    userId: String,
  ): TachiyomiReadProgressDto {
    val indexedReadProgress =
      dsl
        .select(
          rowNumber().over().orderBy(rlb.NUMBER),
          r.COMPLETED,
        ).from(b)
        .leftJoin(r)
        .on(b.ID.eq(r.BOOK_ID))
        .and(readProgressCondition(userId))
        .leftJoin(rlb)
        .on(b.ID.eq(rlb.BOOK_ID))
        .where(rlb.READLIST_ID.eq(readListId))
        .orderBy(rlb.NUMBER)
        .fetch()
        .toList()

    val booksCountRecord =
      dsl
        .select(countUnread.`as`(BOOKS_UNREAD_COUNT))
        .select(countRead.`as`(BOOKS_READ_COUNT))
        .select(countInProgress.`as`(BOOKS_IN_PROGRESS_COUNT))
        .from(b)
        .leftJoin(r)
        .on(b.ID.eq(r.BOOK_ID))
        .and(readProgressCondition(userId))
        .leftJoin(rlb)
        .on(b.ID.eq(rlb.BOOK_ID))
        .where(rlb.READLIST_ID.eq(readListId))
        .fetch()
        .first()

    val booksCount =
      BooksCount(
        unreadCount = booksCountRecord.get(BOOKS_UNREAD_COUNT, Int::class.java),
        readCount = booksCountRecord.get(BOOKS_READ_COUNT, Int::class.java),
        inProgressCount = booksCountRecord.get(BOOKS_IN_PROGRESS_COUNT, Int::class.java),
      )

    return booksCountToDto(booksCount, indexedReadProgress.lastRead() ?: 0)
  }

  private fun booksCountToDto(
    booksCount: BooksCount,
    lastReadContinuousIndex: Int,
  ): TachiyomiReadProgressDto =
    TachiyomiReadProgressDto(
      booksCount = booksCount.totalCount,
      booksUnreadCount = booksCount.unreadCount,
      booksInProgressCount = booksCount.inProgressCount,
      booksReadCount = booksCount.readCount,
      lastReadContinuousIndex = lastReadContinuousIndex,
    )

  private fun booksCountToDtoV2(
    booksCount: BooksCount,
    lastReadContinuousNumberSort: Float,
    maxNumberSort: Float,
  ): TachiyomiReadProgressV2Dto =
    TachiyomiReadProgressV2Dto(
      booksCount = booksCount.totalCount,
      booksUnreadCount = booksCount.unreadCount,
      booksInProgressCount = booksCount.inProgressCount,
      booksReadCount = booksCount.readCount,
      lastReadContinuousNumberSort = lastReadContinuousNumberSort,
      maxNumberSort = maxNumberSort,
    )

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)

  private fun <T> List<Record2<T, Boolean>>.lastRead(): T? =
    this
      .takeWhile { it.component2() == true }
      .lastOrNull()
      ?.component1()

  private data class BooksCount(
    val unreadCount: Int,
    val readCount: Int,
    val inProgressCount: Int,
  ) {
    val totalCount
      get() = unreadCount + readCount + inProgressCount
  }
}
