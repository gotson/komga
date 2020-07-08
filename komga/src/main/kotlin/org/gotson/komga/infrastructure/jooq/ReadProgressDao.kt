package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.ReadProgressRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ReadProgressDao(
  private val dsl: DSLContext
) : ReadProgressRepository {

  private val r = Tables.READ_PROGRESS

  override fun findAll(): Collection<ReadProgress> =
    dsl.selectFrom(r)
      .fetchInto(r)
      .map { it.toDomain() }

  override fun findByBookIdAndUserId(bookId: Long, userId: Long): ReadProgress? =
    dsl.selectFrom(r)
      .where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId)))
      .fetchOneInto(r)
      ?.toDomain()

  override fun findByUserId(userId: Long): Collection<ReadProgress> =
    dsl.selectFrom(r)
      .where(r.USER_ID.eq(userId))
      .fetchInto(r)
      .map { it.toDomain() }


  override fun save(readProgress: ReadProgress) {
//    dsl.mergeInto(r)
//      .using(dsl.selectOne())
//      .on(r.BOOK_ID.eq(readProgress.bookId).and(r.USER_ID.eq(readProgress.userId)))
//      .whenMatchedThenUpdate()
//      .set(r.PAGE, readProgress.page)
//      .set(r.COMPLETED, readProgress.completed)
//      .set(r.LAST_MODIFIED_DATE, LocalDateTime.now())
//      .whenNotMatchedThenInsert()
//      .set(r.BOOK_ID, readProgress.bookId)
//      .set(r.USER_ID, readProgress.userId)
//      .set(r.PAGE, readProgress.page)
//      .set(r.COMPLETED, readProgress.completed)
//      .execute()

//    val exists = dsl.fetchExists(
//      dsl.selectOne()
//        .from(r)
//        .where(r.BOOK_ID.eq(readProgress.bookId))
//        .and(r.USER_ID.eq(readProgress.userId))
//    )
//    if (exists) {
//      dsl.insertInto(r, r.BOOK_ID, r.USER_ID, r.PAGE, r.COMPLETED)
//        .values(readProgress.bookId, readProgress.userId, readProgress.page, readProgress.completed)
//        .execute()
//    } else {
//      dsl.update(r)
//        .set(r.PAGE, readProgress.page)
//        .set(r.COMPLETED, readProgress.completed)
//        .set(r.LAST_MODIFIED_DATE, LocalDateTime.now())
//        .execute()
//    }

    dsl.insertInto(r, r.BOOK_ID, r.USER_ID, r.PAGE, r.COMPLETED)
      .values(readProgress.bookId, readProgress.userId, readProgress.page, readProgress.completed)
      .onDuplicateKeyUpdate()
      .set(r.PAGE, readProgress.page)
      .set(r.COMPLETED, readProgress.completed)
      .set(r.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .execute()
  }


  override fun delete(bookId: Long, userId: Long) {
    dsl.deleteFrom(r)
      .where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId)))
      .execute()
  }

  override fun deleteByUserId(userId: Long) {
    dsl.deleteFrom(r)
      .where(r.USER_ID.eq(userId))
      .execute()
  }

  override fun deleteByBookId(bookId: Long) {
    dsl.deleteFrom(r)
      .where(r.BOOK_ID.eq(bookId))
      .execute()
  }

  override fun deleteByBookIds(bookIds: Collection<Long>) {
    dsl.deleteFrom(r)
      .where(r.BOOK_ID.`in`(bookIds))
      .execute()
  }

  override fun deleteAll() {
    dsl.deleteFrom(r).execute()
  }


  private fun ReadProgressRecord.toDomain() =
    ReadProgress(
      bookId = bookId,
      userId = userId,
      page = page,
      completed = completed,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone()
    )
}
