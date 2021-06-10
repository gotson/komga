package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressDto
import org.gotson.komga.interfaces.rest.persistence.ReadProgressDtoRepository
import org.gotson.komga.jooq.Tables
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Record2
import org.jooq.impl.DSL.rowNumber
import org.springframework.stereotype.Component

@Component
class ReadProgressDtoDao(
  private val dsl: DSLContext
) : ReadProgressDtoRepository {

  private val rlb = Tables.READLIST_BOOK
  private val b = Tables.BOOK
  private val d = Tables.BOOK_METADATA
  private val r = Tables.READ_PROGRESS

  override fun findProgressBySeries(seriesId: String, userId: String): TachiyomiReadProgressDto {
    val indexedReadProgress = dsl.select(
      rowNumber().over().orderBy(d.NUMBER_SORT),
      r.COMPLETED,
    )
      .from(b)
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(d.NUMBER_SORT)
      .fetch()
      .toList()

    val booksCountRecord = dsl
      .select(SeriesDtoDao.countUnread.`as`(BOOKS_UNREAD_COUNT))
      .select(SeriesDtoDao.countRead.`as`(BOOKS_READ_COUNT))
      .select(SeriesDtoDao.countInProgress.`as`(BOOKS_IN_PROGRESS_COUNT))
      .from(b)
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
      .where(b.SERIES_ID.eq(seriesId))
      .fetch()
      .first()

    return booksCountToDto(booksCountRecord, indexedReadProgress)
  }

  override fun findProgressByReadList(readListId: String, userId: String): TachiyomiReadProgressDto {
    val indexedReadProgress = dsl.select(
      rowNumber().over().orderBy(rlb.NUMBER),
      r.COMPLETED,
    )
      .from(b)
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
      .leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID))
      .where(rlb.READLIST_ID.eq(readListId))
      .orderBy(rlb.NUMBER)
      .fetch()
      .toList()

    val booksCountRecord = dsl
      .select(SeriesDtoDao.countUnread.`as`(BOOKS_UNREAD_COUNT))
      .select(SeriesDtoDao.countRead.`as`(BOOKS_READ_COUNT))
      .select(SeriesDtoDao.countInProgress.`as`(BOOKS_IN_PROGRESS_COUNT))
      .from(b)
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
      .leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID))
      .where(rlb.READLIST_ID.eq(readListId))
      .fetch()
      .first()

    return booksCountToDto(booksCountRecord, indexedReadProgress)
  }

  private fun booksCountToDto(booksCountRecord: Record, indexedReadProgress: List<Record2<Int, Boolean>>): TachiyomiReadProgressDto {
    val booksUnreadCount = booksCountRecord.get(BOOKS_UNREAD_COUNT, Int::class.java)
    val booksReadCount = booksCountRecord.get(BOOKS_READ_COUNT, Int::class.java)
    val booksInProgressCount = booksCountRecord.get(BOOKS_IN_PROGRESS_COUNT, Int::class.java)

    val lastReadContinuousIndex = indexedReadProgress
      .takeWhile { it.component2() == true }
      .lastOrNull()
      ?.component1() ?: 0

    return TachiyomiReadProgressDto(
      booksCount = booksUnreadCount + booksReadCount + booksInProgressCount,
      booksUnreadCount = booksUnreadCount,
      booksInProgressCount = booksInProgressCount,
      booksReadCount = booksReadCount,
      lastReadContinuousIndex = lastReadContinuousIndex,
    )
  }

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)
}
