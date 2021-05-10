package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.infrastructure.jooq.BOOKS_IN_PROGRESS_COUNT
import org.gotson.komga.infrastructure.jooq.BOOKS_READ_COUNT
import org.gotson.komga.infrastructure.jooq.BOOKS_UNREAD_COUNT
import org.gotson.komga.infrastructure.jooq.SeriesDtoDao
import org.gotson.komga.interfaces.rest.persistence.ReadListDtoRepository
import org.gotson.komga.jooq.Tables
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class ReadListDtoDao(
  private val dsl: DSLContext
) : ReadListDtoRepository {

  private val rl = Tables.READLIST
  private val rlb = Tables.READLIST_BOOK
  private val b = Tables.BOOK
  private val r = Tables.READ_PROGRESS

  override fun getProgress(readListId: String, userId: String): ReadListProgressDto {

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

    val booksUnreadCount = booksCountRecord.get(BOOKS_UNREAD_COUNT, Int::class.java)
    val booksReadCount = booksCountRecord.get(BOOKS_READ_COUNT, Int::class.java)
    val booksInProgressCount = booksCountRecord.get(BOOKS_IN_PROGRESS_COUNT, Int::class.java)

    return ReadListProgressDto(
      booksCount = booksUnreadCount + booksReadCount + booksInProgressCount,
      booksUnreadCount = booksUnreadCount,
      booksInProgressCount = booksInProgressCount,
      booksReadCount = booksReadCount,
    )
  }

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)
}
