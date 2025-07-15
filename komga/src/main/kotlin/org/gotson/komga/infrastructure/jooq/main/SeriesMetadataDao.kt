package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.AlternateTitle
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.WebLink
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.SeriesMetadataRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SeriesMetadataDao(
  private val dsl: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SeriesMetadataRepository {
  private val d = Tables.SERIES_METADATA
  private val g = Tables.SERIES_METADATA_GENRE
  private val st = Tables.SERIES_METADATA_TAG
  private val sl = Tables.SERIES_METADATA_SHARING
  private val slk = Tables.SERIES_METADATA_LINK
  private val sat = Tables.SERIES_METADATA_ALTERNATE_TITLE

  override fun findById(seriesId: String): SeriesMetadata = findOne(seriesId)!!.toDomain(findGenres(seriesId), findTags(seriesId), findSharingLabels(seriesId), findLinks(seriesId), findAlternateTitles(seriesId))

  override fun findByIdOrNull(seriesId: String): SeriesMetadata? = findOne(seriesId)?.toDomain(findGenres(seriesId), findTags(seriesId), findSharingLabels(seriesId), findLinks(seriesId), findAlternateTitles(seriesId))

  private fun findOne(seriesId: String) =
    dsl
      .selectFrom(d)
      .where(d.SERIES_ID.eq(seriesId))
      .fetchOneInto(d)

  private fun findGenres(seriesId: String) =
    dsl
      .select(g.GENRE)
      .from(g)
      .where(g.SERIES_ID.eq(seriesId))
      .fetchSet(g.GENRE)

  private fun findTags(seriesId: String) =
    dsl
      .select(st.TAG)
      .from(st)
      .where(st.SERIES_ID.eq(seriesId))
      .fetchSet(st.TAG)

  private fun findSharingLabels(seriesId: String) =
    dsl
      .select(sl.LABEL)
      .from(sl)
      .where(sl.SERIES_ID.eq(seriesId))
      .fetchSet(sl.LABEL)

  private fun findLinks(seriesId: String) =
    dsl
      .select(slk.LABEL, slk.URL)
      .from(slk)
      .where(slk.SERIES_ID.eq(seriesId))
      .fetchInto(slk)
      .map { WebLink(it.label, URI(it.url)) }

  private fun findAlternateTitles(seriesId: String) =
    dsl
      .select(sat.LABEL, sat.TITLE)
      .from(sat)
      .where(sat.SERIES_ID.eq(seriesId))
      .fetchInto(sat)
      .map { AlternateTitle(it.label, it.title) }

  @Transactional
  override fun insert(metadata: SeriesMetadata) {
    dsl
      .insertInto(d)
      .set(d.SERIES_ID, metadata.seriesId)
      .set(d.STATUS, metadata.status.toString())
      .set(d.TITLE, metadata.title)
      .set(d.TITLE_SORT, metadata.titleSort)
      .set(d.SUMMARY, metadata.summary)
      .set(d.READING_DIRECTION, metadata.readingDirection?.toString())
      .set(d.PUBLISHER, metadata.publisher)
      .set(d.AGE_RATING, metadata.ageRating)
      .set(d.LANGUAGE, metadata.language)
      .set(d.STATUS_LOCK, metadata.statusLock)
      .set(d.TITLE_LOCK, metadata.titleLock)
      .set(d.TITLE_SORT_LOCK, metadata.titleSortLock)
      .set(d.SUMMARY_LOCK, metadata.summaryLock)
      .set(d.READING_DIRECTION_LOCK, metadata.readingDirectionLock)
      .set(d.PUBLISHER_LOCK, metadata.publisherLock)
      .set(d.AGE_RATING_LOCK, metadata.ageRatingLock)
      .set(d.LANGUAGE_LOCK, metadata.languageLock)
      .set(d.GENRES_LOCK, metadata.genresLock)
      .set(d.TAGS_LOCK, metadata.tagsLock)
      .set(d.TOTAL_BOOK_COUNT, metadata.totalBookCount)
      .set(d.TOTAL_BOOK_COUNT_LOCK, metadata.totalBookCountLock)
      .set(d.SHARING_LABELS_LOCK, metadata.sharingLabelsLock)
      .set(d.LINKS_LOCK, metadata.linksLock)
      .set(d.ALTERNATE_TITLES_LOCK, metadata.alternateTitlesLock)
      .execute()

    insertGenres(metadata)
    insertTags(metadata)
    insertSharingLabels(metadata)
    insertLinks(metadata)
    insertAlternateTitles(metadata)
  }

  @Transactional
  override fun update(metadata: SeriesMetadata) {
    dsl
      .update(d)
      .set(d.STATUS, metadata.status.toString())
      .set(d.TITLE, metadata.title)
      .set(d.TITLE_SORT, metadata.titleSort)
      .set(d.SUMMARY, metadata.summary)
      .set(d.READING_DIRECTION, metadata.readingDirection?.toString())
      .set(d.PUBLISHER, metadata.publisher)
      .set(d.AGE_RATING, metadata.ageRating)
      .set(d.LANGUAGE, metadata.language)
      .set(d.STATUS_LOCK, metadata.statusLock)
      .set(d.TITLE_LOCK, metadata.titleLock)
      .set(d.TITLE_SORT_LOCK, metadata.titleSortLock)
      .set(d.SUMMARY_LOCK, metadata.summaryLock)
      .set(d.READING_DIRECTION_LOCK, metadata.readingDirectionLock)
      .set(d.PUBLISHER_LOCK, metadata.publisherLock)
      .set(d.AGE_RATING_LOCK, metadata.ageRatingLock)
      .set(d.LANGUAGE_LOCK, metadata.languageLock)
      .set(d.GENRES_LOCK, metadata.genresLock)
      .set(d.TAGS_LOCK, metadata.tagsLock)
      .set(d.TOTAL_BOOK_COUNT, metadata.totalBookCount)
      .set(d.TOTAL_BOOK_COUNT_LOCK, metadata.totalBookCountLock)
      .set(d.SHARING_LABELS_LOCK, metadata.sharingLabelsLock)
      .set(d.LINKS_LOCK, metadata.linksLock)
      .set(d.ALTERNATE_TITLES_LOCK, metadata.alternateTitlesLock)
      .set(d.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(d.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dsl
      .deleteFrom(g)
      .where(g.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dsl
      .deleteFrom(st)
      .where(st.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dsl
      .deleteFrom(sl)
      .where(sl.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dsl
      .deleteFrom(slk)
      .where(slk.SERIES_ID.eq(metadata.seriesId))
      .execute()

    dsl
      .deleteFrom(sat)
      .where(sat.SERIES_ID.eq(metadata.seriesId))
      .execute()

    insertGenres(metadata)
    insertTags(metadata)
    insertSharingLabels(metadata)
    insertLinks(metadata)
    insertAlternateTitles(metadata)
  }

  private fun insertGenres(metadata: SeriesMetadata) {
    if (metadata.genres.isNotEmpty()) {
      metadata.genres.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(g, g.SERIES_ID, g.GENRE)
              .values(null as String?, null),
          ).also { step ->
            chunk.forEach {
              step.bind(metadata.seriesId, it)
            }
          }.execute()
      }
    }
  }

  private fun insertTags(metadata: SeriesMetadata) {
    if (metadata.tags.isNotEmpty()) {
      metadata.tags.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(st, st.SERIES_ID, st.TAG)
              .values(null as String?, null),
          ).also { step ->
            chunk.forEach {
              step.bind(metadata.seriesId, it)
            }
          }.execute()
      }
    }
  }

  private fun insertSharingLabels(metadata: SeriesMetadata) {
    if (metadata.sharingLabels.isNotEmpty()) {
      metadata.sharingLabels.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(sl, sl.SERIES_ID, sl.LABEL)
              .values(null as String?, null),
          ).also { step ->
            chunk.forEach {
              step.bind(metadata.seriesId, it)
            }
          }.execute()
      }
    }
  }

  private fun insertLinks(metadata: SeriesMetadata) {
    if (metadata.links.isNotEmpty()) {
      metadata.links.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(slk, slk.SERIES_ID, slk.LABEL, slk.URL)
              .values(null as String?, null, null),
          ).also { step ->
            chunk.forEach {
              step.bind(metadata.seriesId, it.label, it.url.toString())
            }
          }.execute()
      }
    }
  }

  private fun insertAlternateTitles(metadata: SeriesMetadata) {
    if (metadata.alternateTitles.isNotEmpty()) {
      metadata.alternateTitles.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(sat, sat.SERIES_ID, sat.LABEL, sat.TITLE)
              .values(null as String?, null, null),
          ).also { step ->
            chunk.forEach {
              step.bind(metadata.seriesId, it.label, it.title)
            }
          }.execute()
      }
    }
  }

  @Transactional
  override fun delete(seriesId: String) {
    dsl.deleteFrom(g).where(g.SERIES_ID.eq(seriesId)).execute()
    dsl.deleteFrom(st).where(st.SERIES_ID.eq(seriesId)).execute()
    dsl.deleteFrom(sl).where(sl.SERIES_ID.eq(seriesId)).execute()
    dsl.deleteFrom(slk).where(slk.SERIES_ID.eq(seriesId)).execute()
    dsl.deleteFrom(sat).where(sat.SERIES_ID.eq(seriesId)).execute()
    dsl.deleteFrom(d).where(d.SERIES_ID.eq(seriesId)).execute()
  }

  @Transactional
  override fun delete(seriesIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, seriesIds)

    dsl.deleteFrom(g).where(g.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(st).where(st.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(sl).where(sl.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(slk).where(slk.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(sat).where(sat.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(d).where(d.SERIES_ID.`in`(dsl.selectTempStrings())).execute()
  }

  override fun count(): Long = dsl.fetchCount(d).toLong()

  private fun SeriesMetadataRecord.toDomain(
    genres: Set<String>,
    tags: Set<String>,
    sharingLabels: Set<String>,
    links: List<WebLink>,
    alternateTitles: List<AlternateTitle>,
  ) = SeriesMetadata(
    status = SeriesMetadata.Status.valueOf(status),
    title = title,
    titleSort = titleSort,
    summary = summary,
    readingDirection =
      readingDirection?.let {
        SeriesMetadata.ReadingDirection.valueOf(readingDirection)
      },
    publisher = publisher,
    ageRating = ageRating,
    language = language,
    genres = genres,
    tags = tags,
    totalBookCount = totalBookCount,
    sharingLabels = sharingLabels,
    links = links,
    alternateTitles = alternateTitles,
    statusLock = statusLock,
    titleLock = titleLock,
    titleSortLock = titleSortLock,
    summaryLock = summaryLock,
    readingDirectionLock = readingDirectionLock,
    publisherLock = publisherLock,
    ageRatingLock = ageRatingLock,
    languageLock = languageLock,
    genresLock = genresLock,
    tagsLock = tagsLock,
    totalBookCountLock = totalBookCountLock,
    sharingLabelsLock = sharingLabelsLock,
    linksLock = linksLock,
    alternateTitlesLock = alternateTitlesLock,
    seriesId = seriesId,
    createdDate = createdDate.toCurrentTimeZone(),
    lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
  )
}
