package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.ReadProgressRecord
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ReadProgressDao(
  private val dsl: DSLContext
) : ReadProgressRepository {

  private val r = Tables.READ_PROGRESS
  private val rs = Tables.READ_PROGRESS_SERIES
  private val b = Tables.BOOK

  override fun findAll(): Collection<ReadProgress> =
    dsl.selectFrom(r)
      .fetchInto(r)
      .map { it.toDomain() }

  override fun findByBookIdAndUserIdOrNull(bookId: String, userId: String): ReadProgress? =
    dsl.selectFrom(r)
      .where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId)))
      .fetchOneInto(r)
      ?.toDomain()

  override fun findAllByUserId(userId: String): Collection<ReadProgress> =
    dsl.selectFrom(r)
      .where(r.USER_ID.eq(userId))
      .fetchInto(r)
      .map { it.toDomain() }

  override fun findAllByBookId(bookId: String): Collection<ReadProgress> =
    dsl.selectFrom(r)
      .where(r.BOOK_ID.eq(bookId))
      .fetchInto(r)
      .map { it.toDomain() }

  override fun findAllByBookIdsAndUserId(bookIds: Collection<String>, userId: String): Collection<ReadProgress> =
    dsl.selectFrom(r)
      .where(r.BOOK_ID.`in`(bookIds).and(r.USER_ID.eq(userId)))
      .fetchInto(r)
      .map { it.toDomain() }

  override fun save(readProgress: ReadProgress) {
    dsl.transaction { config ->
      config.dsl().saveQuery(readProgress).execute()
      config.dsl().aggregateSeriesProgress(listOf(readProgress.bookId), readProgress.userId)
    }
  }

  override fun save(readProgresses: Collection<ReadProgress>) {
    dsl.transaction { config ->
      val queries = readProgresses.map { config.dsl().saveQuery(it) }
      config.dsl().batch(queries).execute()

      readProgresses.groupBy { it.userId }
        .forEach { (userId, readProgresses) ->
          config.dsl().aggregateSeriesProgress(readProgresses.map { it.bookId }, userId)
        }
    }
  }

  private fun DSLContext.saveQuery(readProgress: ReadProgress): Query =
    this.insertInto(r, r.BOOK_ID, r.USER_ID, r.PAGE, r.COMPLETED)
      .values(readProgress.bookId, readProgress.userId, readProgress.page, readProgress.completed)
      .onDuplicateKeyUpdate()
      .set(r.PAGE, readProgress.page)
      .set(r.COMPLETED, readProgress.completed)
      .set(r.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))

  override fun delete(bookId: String, userId: String) {
    dsl.transaction { config ->
      config.dsl().deleteFrom(r).where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId))).execute()
      config.dsl().aggregateSeriesProgress(listOf(bookId), userId)
    }
  }

  override fun deleteByUserId(userId: String) {
    dsl.transaction { config ->
      config.dsl().deleteFrom(r).where(r.USER_ID.eq(userId)).execute()
      config.dsl().deleteFrom(rs).where(rs.USER_ID.eq(userId)).execute()
    }
  }

  override fun deleteByBookId(bookId: String) {
    dsl.transaction { config ->
      config.dsl().deleteFrom(r).where(r.BOOK_ID.eq(bookId)).execute()
      config.dsl().aggregateSeriesProgress(listOf(bookId))
    }
  }

  override fun deleteByBookIds(bookIds: Collection<String>) {
    dsl.transaction { config ->
      config.dsl().deleteFrom(r).where(r.BOOK_ID.`in`(bookIds)).execute()
      config.dsl().aggregateSeriesProgress(bookIds)
    }
  }

  override fun deleteByBookIdsAndUserId(bookIds: Collection<String>, userId: String) {
    dsl.transaction { config ->
      config.dsl().deleteFrom(r).where(r.BOOK_ID.`in`(bookIds)).and(r.USER_ID.eq(userId)).execute()
      config.dsl().aggregateSeriesProgress(bookIds, userId)
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      config.dsl().deleteFrom(r).execute()
      config.dsl().deleteFrom(rs).execute()
    }
  }

  private fun DSLContext.aggregateSeriesProgress(bookIds: Collection<String>, userId: String? = null) {
    val seriesIds = this.select(b.SERIES_ID)
      .from(b)
      .where(b.ID.`in`(bookIds))
      .fetch(b.SERIES_ID)

    this.deleteFrom(rs)
      .where(rs.SERIES_ID.`in`(seriesIds))
      .apply { userId?.let { and(rs.USER_ID.eq(it)) } }
      .execute()

    this.insertInto(rs)
      .select(
        this.select(b.SERIES_ID, r.USER_ID)
          .select(DSL.sum(DSL.`when`(r.COMPLETED.isTrue, 1).otherwise(0)))
          .select(DSL.sum(DSL.`when`(r.COMPLETED.isFalse, 1).otherwise(0)))
          .from(b)
          .innerJoin(r).on(b.ID.eq(r.BOOK_ID))
          .where(b.SERIES_ID.`in`(seriesIds))
          .apply { userId?.let { and(r.USER_ID.eq(it)) } }
          .groupBy(b.SERIES_ID, r.USER_ID)
      ).execute()
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
