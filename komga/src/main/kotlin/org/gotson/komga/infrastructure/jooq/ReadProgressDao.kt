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

  override fun findByBookIdAndUserId(bookId: String, userId: String): ReadProgress? =
    dsl.selectFrom(r)
      .where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId)))
      .fetchOneInto(r)
      ?.toDomain()

  override fun findByUserId(userId: String): Collection<ReadProgress> =
    dsl.selectFrom(r)
      .where(r.USER_ID.eq(userId))
      .fetchInto(r)
      .map { it.toDomain() }


  override fun save(readProgress: ReadProgress) {
    dsl.insertInto(r, r.BOOK_ID, r.USER_ID, r.PAGE, r.COMPLETED)
      .values(readProgress.bookId, readProgress.userId, readProgress.page, readProgress.completed)
      .onDuplicateKeyUpdate()
      .set(r.PAGE, readProgress.page)
      .set(r.COMPLETED, readProgress.completed)
      .set(r.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .execute()
  }


  override fun delete(bookId: String, userId: String) {
    dsl.deleteFrom(r)
      .where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId)))
      .execute()
  }

  override fun deleteByUserId(userId: String) {
    dsl.deleteFrom(r)
      .where(r.USER_ID.eq(userId))
      .execute()
  }

  override fun deleteByBookId(bookId: String) {
    dsl.deleteFrom(r)
      .where(r.BOOK_ID.eq(bookId))
      .execute()
  }

  override fun deleteByBookIds(bookIds: Collection<String>) {
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
