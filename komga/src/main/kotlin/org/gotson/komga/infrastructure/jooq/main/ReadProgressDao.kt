package org.gotson.komga.infrastructure.jooq.main

import com.fasterxml.jackson.databind.ObjectMapper
import org.gotson.komga.domain.model.R2Locator
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.infrastructure.jooq.deserializeJsonGz
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.infrastructure.jooq.serializeJsonGz
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.ReadProgressRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.gotson.komga.language.toUTC
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ReadProgressDao(
  private val dsl: DSLContext,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
  private val mapper: ObjectMapper,
) : ReadProgressRepository {
  private val r = Tables.READ_PROGRESS
  private val rs = Tables.READ_PROGRESS_SERIES
  private val b = Tables.BOOK

  override fun findAll(): Collection<ReadProgress> =
    dsl
      .selectFrom(r)
      .fetchInto(r)
      .map { it.toDomain() }

  override fun findByBookIdAndUserIdOrNull(
    bookId: String,
    userId: String,
  ): ReadProgress? =
    dsl
      .selectFrom(r)
      .where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId)))
      .fetchOneInto(r)
      ?.toDomain()

  override fun findAllByUserId(userId: String): Collection<ReadProgress> =
    dsl
      .selectFrom(r)
      .where(r.USER_ID.eq(userId))
      .fetchInto(r)
      .map { it.toDomain() }

  override fun findAllByBookId(bookId: String): Collection<ReadProgress> =
    dsl
      .selectFrom(r)
      .where(r.BOOK_ID.eq(bookId))
      .fetchInto(r)
      .map { it.toDomain() }

  override fun findAllByBookIdsAndUserId(
    bookIds: Collection<String>,
    userId: String,
  ): Collection<ReadProgress> =
    dsl
      .selectFrom(r)
      .where(r.BOOK_ID.`in`(bookIds).and(r.USER_ID.eq(userId)))
      .fetchInto(r)
      .map { it.toDomain() }

  @Transactional
  override fun save(readProgress: ReadProgress) {
    readProgress.toQuery().execute()
    aggregateSeriesProgress(listOf(readProgress.bookId), readProgress.userId)
  }

  @Transactional
  override fun save(readProgresses: Collection<ReadProgress>) {
    readProgresses
      .map { it.toQuery() }
      .chunked(batchSize)
      .forEach { chunk -> dsl.batch(chunk).execute() }

    readProgresses
      .groupBy { it.userId }
      .forEach { (userId, readProgresses) ->
        aggregateSeriesProgress(readProgresses.map { it.bookId }, userId)
      }
  }

  private fun ReadProgress.toQuery(): Query =
    dsl
      .insertInto(
        r,
        r.BOOK_ID,
        r.USER_ID,
        r.PAGE,
        r.COMPLETED,
        r.READ_DATE,
        r.DEVICE_ID,
        r.DEVICE_NAME,
        r.LOCATOR,
      ).values(
        bookId,
        userId,
        page,
        completed,
        readDate.toUTC(),
        deviceId,
        deviceName,
        locator?.let { mapper.serializeJsonGz(it) },
      ).onDuplicateKeyUpdate()
      .set(r.PAGE, page)
      .set(r.COMPLETED, completed)
      .set(r.READ_DATE, readDate.toUTC())
      .set(r.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .set(r.DEVICE_ID, deviceId)
      .set(r.DEVICE_NAME, deviceName)
      .set(r.LOCATOR, locator?.let { mapper.serializeJsonGz(it) })

  @Transactional
  override fun delete(
    bookId: String,
    userId: String,
  ) {
    dsl.deleteFrom(r).where(r.BOOK_ID.eq(bookId).and(r.USER_ID.eq(userId))).execute()
    aggregateSeriesProgress(listOf(bookId), userId)
  }

  @Transactional
  override fun deleteByUserId(userId: String) {
    dsl.deleteFrom(r).where(r.USER_ID.eq(userId)).execute()
    dsl.deleteFrom(rs).where(rs.USER_ID.eq(userId)).execute()
  }

  @Transactional
  override fun deleteByBookId(bookId: String) {
    dsl.deleteFrom(r).where(r.BOOK_ID.eq(bookId)).execute()
    aggregateSeriesProgress(listOf(bookId))
  }

  @Transactional
  override fun deleteByBookIds(bookIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, bookIds)

    dsl.deleteFrom(r).where(r.BOOK_ID.`in`(dsl.selectTempStrings())).execute()
    aggregateSeriesProgress(bookIds)
  }

  @Transactional
  override fun deleteBySeriesIds(seriesIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, seriesIds)

    dsl.deleteFrom(rs).where(rs.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
  }

  @Transactional
  override fun deleteByBookIdsAndUserId(
    bookIds: Collection<String>,
    userId: String,
  ) {
    dsl.insertTempStrings(batchSize, bookIds)

    dsl
      .deleteFrom(r)
      .where(r.BOOK_ID.`in`(dsl.selectTempStrings()))
      .and(r.USER_ID.eq(userId))
      .execute()
    aggregateSeriesProgress(bookIds, userId)
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(r).execute()
    dsl.deleteFrom(rs).execute()
  }

  private fun aggregateSeriesProgress(
    bookIds: Collection<String>,
    userId: String? = null,
  ) {
    dsl.insertTempStrings(batchSize, bookIds)

    val seriesIdsQuery =
      dsl
        .select(b.SERIES_ID)
        .from(b)
        .where(b.ID.`in`(dsl.selectTempStrings()))

    dsl
      .deleteFrom(rs)
      .where(rs.SERIES_ID.`in`(seriesIdsQuery))
      .apply { userId?.let { and(rs.USER_ID.eq(it)) } }
      .execute()

    dsl
      .insertInto(rs)
      .select(
        dsl
          .select(b.SERIES_ID, r.USER_ID)
          .select(DSL.sum(DSL.`when`(r.COMPLETED.isTrue, 1).otherwise(0)))
          .select(DSL.sum(DSL.`when`(r.COMPLETED.isFalse, 1).otherwise(0)))
          .select(DSL.max(r.READ_DATE))
          .select(DSL.currentTimestamp())
          .from(b)
          .innerJoin(r)
          .on(b.ID.eq(r.BOOK_ID))
          .where(b.SERIES_ID.`in`(seriesIdsQuery))
          .apply { userId?.let { and(r.USER_ID.eq(it)) } }
          .groupBy(b.SERIES_ID, r.USER_ID),
      ).execute()
  }

  private fun ReadProgressRecord.toDomain() =
    ReadProgress(
      bookId = bookId,
      userId = userId,
      page = page,
      completed = completed,
      readDate = readDate.toCurrentTimeZone(),
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      deviceId = deviceId,
      deviceName = deviceName,
      locator = mapper.deserializeJsonGz<R2Locator>(locator),
    )
}
