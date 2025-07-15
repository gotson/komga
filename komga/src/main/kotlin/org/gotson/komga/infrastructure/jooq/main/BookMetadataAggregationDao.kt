package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataAggregation
import org.gotson.komga.domain.persistence.BookMetadataAggregationRepository
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookMetadataAggregationAuthorRecord
import org.gotson.komga.jooq.main.tables.records.BookMetadataAggregationRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class BookMetadataAggregationDao(
  private val dsl: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : BookMetadataAggregationRepository {
  private val d = Tables.BOOK_METADATA_AGGREGATION
  private val a = Tables.BOOK_METADATA_AGGREGATION_AUTHOR
  private val t = Tables.BOOK_METADATA_AGGREGATION_TAG

  override fun findById(seriesId: String): BookMetadataAggregation = findOne(listOf(seriesId)).first()

  override fun findByIdOrNull(seriesId: String): BookMetadataAggregation? = findOne(listOf(seriesId)).firstOrNull()

  private fun findOne(seriesIds: Collection<String>) =
    dsl
      .select(*d.fields(), *a.fields())
      .from(d)
      .leftJoin(a)
      .on(d.SERIES_ID.eq(a.SERIES_ID))
      .where(d.SERIES_ID.`in`(seriesIds))
      .fetchGroups(
        { it.into(d) },
        { it.into(a) },
      ).map { (dr, ar) ->
        dr.toDomain(ar.filterNot { it.name == null }.map { it.toDomain() }, findTags(dr.seriesId))
      }

  private fun findTags(seriesId: String) =
    dsl
      .select(t.TAG)
      .from(t)
      .where(t.SERIES_ID.eq(seriesId))
      .fetchSet(t.TAG)

  @Transactional
  override fun insert(metadata: BookMetadataAggregation) {
    dsl
      .insertInto(d)
      .set(d.SERIES_ID, metadata.seriesId)
      .set(d.RELEASE_DATE, metadata.releaseDate)
      .set(d.SUMMARY, metadata.summary)
      .set(d.SUMMARY_NUMBER, metadata.summaryNumber)
      .execute()

    insertAuthors(metadata)
    insertTags(metadata)
  }

  @Transactional
  override fun update(metadata: BookMetadataAggregation) {
    dsl
      .update(d)
      .set(d.SUMMARY, metadata.summary)
      .set(d.SUMMARY_NUMBER, metadata.summaryNumber)
      .set(d.RELEASE_DATE, metadata.releaseDate)
      .set(d.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(d.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dsl
      .deleteFrom(a)
      .where(a.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dsl
      .deleteFrom(t)
      .where(t.SERIES_ID.eq(metadata.seriesId))
      .execute()

    insertAuthors(metadata)
    insertTags(metadata)
  }

  private fun insertAuthors(metadata: BookMetadataAggregation) {
    if (metadata.authors.isNotEmpty()) {
      metadata.authors.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
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

  private fun insertTags(metadata: BookMetadataAggregation) {
    if (metadata.tags.isNotEmpty()) {
      metadata.tags.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
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
    dsl.deleteFrom(a).where(a.SERIES_ID.eq(seriesId)).execute()
    dsl.deleteFrom(t).where(t.SERIES_ID.eq(seriesId)).execute()
    dsl.deleteFrom(d).where(d.SERIES_ID.eq(seriesId)).execute()
  }

  @Transactional
  override fun delete(seriesIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, seriesIds)

    dsl.deleteFrom(a).where(a.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(t).where(t.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(d).where(d.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
  }

  override fun count(): Long = dsl.fetchCount(d).toLong()

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
