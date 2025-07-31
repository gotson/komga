package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataAggregation
import org.gotson.komga.domain.persistence.BookMetadataAggregationRepository
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookMetadataAggregationAuthorRecord
import org.gotson.komga.jooq.main.tables.records.BookMetadataAggregationRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class BookMetadataAggregationDao(
  private val dslRW: DSLContext,
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : BookMetadataAggregationRepository {
  private val d = Tables.BOOK_METADATA_AGGREGATION
  private val a = Tables.BOOK_METADATA_AGGREGATION_AUTHOR
  private val t = Tables.BOOK_METADATA_AGGREGATION_TAG

  override fun findById(seriesId: String): BookMetadataAggregation = dslRO.findOne(listOf(seriesId)).first()

  override fun findByIdOrNull(seriesId: String): BookMetadataAggregation? = dslRO.findOne(listOf(seriesId)).firstOrNull()

  private fun DSLContext.findOne(seriesIds: Collection<String>) =
    this
      .select(*d.fields(), *a.fields())
      .from(d)
      .leftJoin(a)
      .on(d.SERIES_ID.eq(a.SERIES_ID))
      .where(d.SERIES_ID.`in`(seriesIds))
      .fetchGroups(
        { it.into(d) },
        { it.into(a) },
      ).map { (dr, ar) ->
        dr.toDomain(ar.filterNot { it.name == null }.map { it.toDomain() }, this.findTags(dr.seriesId))
      }

  private fun DSLContext.findTags(seriesId: String) =
    this
      .select(t.TAG)
      .from(t)
      .where(t.SERIES_ID.eq(seriesId))
      .fetchSet(t.TAG)

  @Transactional
  override fun insert(metadata: BookMetadataAggregation) {
    dslRW
      .insertInto(d)
      .set(d.SERIES_ID, metadata.seriesId)
      .set(d.RELEASE_DATE, metadata.releaseDate)
      .set(d.SUMMARY, metadata.summary)
      .set(d.SUMMARY_NUMBER, metadata.summaryNumber)
      .execute()

    dslRW.insertAuthors(metadata)
    dslRW.insertTags(metadata)
  }

  @Transactional
  override fun update(metadata: BookMetadataAggregation) {
    dslRW
      .update(d)
      .set(d.SUMMARY, metadata.summary)
      .set(d.SUMMARY_NUMBER, metadata.summaryNumber)
      .set(d.RELEASE_DATE, metadata.releaseDate)
      .set(d.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(d.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dslRW
      .deleteFrom(a)
      .where(a.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dslRW
      .deleteFrom(t)
      .where(t.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dslRW.insertAuthors(metadata)
    dslRW.insertTags(metadata)
  }

  private fun DSLContext.insertAuthors(metadata: BookMetadataAggregation) {
    if (metadata.authors.isNotEmpty()) {
      metadata.authors.chunked(batchSize).forEach { chunk ->
        this
          .batch(
            this
              .insertInto(a, a.SERIES_ID, a.NAME, a.ROLE)
              .values(null as String?, null, null),
          ).also { step ->
            chunk.forEach {
              step.bind(metadata.seriesId, it.name, it.role)
            }
          }.execute()
      }
    }
  }

  private fun DSLContext.insertTags(metadata: BookMetadataAggregation) {
    if (metadata.tags.isNotEmpty()) {
      metadata.tags.chunked(batchSize).forEach { chunk ->
        this
          .batch(
            this
              .insertInto(t, t.SERIES_ID, t.TAG)
              .values(null as String?, null),
          ).also { step ->
            chunk.forEach {
              step.bind(metadata.seriesId, it)
            }
          }.execute()
      }
    }
  }

  @Transactional
  override fun delete(seriesId: String) {
    dslRW.deleteFrom(a).where(a.SERIES_ID.eq(seriesId)).execute()
    dslRW.deleteFrom(t).where(t.SERIES_ID.eq(seriesId)).execute()
    dslRW.deleteFrom(d).where(d.SERIES_ID.eq(seriesId)).execute()
  }

  @Transactional
  override fun delete(seriesIds: Collection<String>) {
    dslRW.withTempTable(batchSize, seriesIds).use {
      dslRW.deleteFrom(a).where(a.SERIES_ID.`in`(it.selectTempStrings())).execute()
      dslRW.deleteFrom(t).where(t.SERIES_ID.`in`(it.selectTempStrings())).execute()
      dslRW.deleteFrom(d).where(d.SERIES_ID.`in`(it.selectTempStrings())).execute()
    }
  }

  override fun count(): Long = dslRO.fetchCount(d).toLong()

  private fun BookMetadataAggregationRecord.toDomain(
    authors: List<Author>,
    tags: Set<String>,
  ) = BookMetadataAggregation(
    authors = authors,
    tags = tags,
    releaseDate = releaseDate,
    summary = summary,
    summaryNumber = summaryNumber,
    seriesId = seriesId,
    createdDate = createdDate.toCurrentTimeZone(),
    lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
  )

  private fun BookMetadataAggregationAuthorRecord.toDomain() =
    Author(
      name = name,
      role = role,
    )
}
