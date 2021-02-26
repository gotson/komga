package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.BookMetadataAggregationAuthorRecord
import org.gotson.komga.jooq.tables.records.BookMetadataAuthorRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ReferentialDao(
  private val dsl: DSLContext
) : ReferentialRepository {

  private val a = Tables.BOOK_METADATA_AUTHOR
  private val sd = Tables.SERIES_METADATA
  private val bma = Tables.BOOK_METADATA_AGGREGATION
  private val bmaa = Tables.BOOK_METADATA_AGGREGATION_AUTHOR
  private val s = Tables.SERIES
  private val b = Tables.BOOK
  private val g = Tables.SERIES_METADATA_GENRE
  private val bt = Tables.BOOK_METADATA_TAG
  private val st = Tables.SERIES_METADATA_TAG
  private val cs = Tables.COLLECTION_SERIES

  override fun findAuthorsByName(search: String): List<Author> =
    dsl.selectDistinct(a.NAME, a.ROLE)
      .from(a)
      .where(a.NAME.containsIgnoreCase(search))
      .orderBy(a.NAME, a.ROLE)
      .fetchInto(a)
      .map { it.toDomain() }

  override fun findAuthorsByNameAndLibrary(search: String, libraryId: String): List<Author> =
    dsl.selectDistinct(bmaa.NAME, bmaa.ROLE)
      .from(bmaa)
      .leftJoin(s).on(bmaa.SERIES_ID.eq(s.ID))
      .where(bmaa.NAME.containsIgnoreCase(search))
      .and(s.LIBRARY_ID.eq(libraryId))
      .orderBy(bmaa.NAME, bmaa.ROLE)
      .fetchInto(bmaa)
      .map { it.toDomain() }

  override fun findAuthorsByNameAndCollection(search: String, collectionId: String): List<Author> =
    dsl.selectDistinct(bmaa.NAME, bmaa.ROLE)
      .from(bmaa)
      .leftJoin(cs).on(bmaa.SERIES_ID.eq(cs.SERIES_ID))
      .where(bmaa.NAME.containsIgnoreCase(search))
      .and(cs.COLLECTION_ID.eq(collectionId))
      .orderBy(bmaa.NAME, bmaa.ROLE)
      .fetchInto(bmaa)
      .map { it.toDomain() }

  override fun findAuthorsByNameAndSeries(search: String, seriesId: String): List<Author> =
    dsl.selectDistinct(bmaa.NAME, bmaa.ROLE)
      .from(bmaa)
      .where(bmaa.NAME.containsIgnoreCase(search))
      .and(bmaa.SERIES_ID.eq(seriesId))
      .orderBy(bmaa.NAME, bmaa.ROLE)
      .fetchInto(bmaa)
      .map { it.toDomain() }

  override fun findAuthorsNamesByName(search: String): List<String> =
    dsl.selectDistinct(a.NAME)
      .from(a)
      .where(a.NAME.containsIgnoreCase(search))
      .orderBy(a.NAME)
      .fetch(a.NAME)

  override fun findAuthorsRoles(): List<String> =
    dsl.selectDistinct(a.ROLE)
      .from(a)
      .orderBy(a.ROLE)
      .fetch(a.ROLE)

  override fun findAllGenres(): Set<String> =
    dsl.selectDistinct(g.GENRE)
      .from(g)
      .orderBy(lower(g.GENRE))
      .fetchSet(g.GENRE)

  override fun findAllGenresByLibrary(libraryId: String): Set<String> =
    dsl.selectDistinct(g.GENRE)
      .from(g)
      .leftJoin(s).on(g.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.eq(libraryId))
      .orderBy(lower(g.GENRE))
      .fetchSet(g.GENRE)

  override fun findAllGenresByCollection(collectionId: String): Set<String> =
    dsl.selectDistinct(g.GENRE)
      .from(g)
      .leftJoin(cs).on(g.SERIES_ID.eq(cs.SERIES_ID))
      .where(cs.COLLECTION_ID.eq(collectionId))
      .orderBy(lower(g.GENRE))
      .fetchSet(g.GENRE)

  override fun findAllTags(): Set<String> =
    dsl.select(bt.TAG.`as`("tag"))
      .from(bt)
      .union(
        select(st.TAG.`as`("tag")).from(st)
      )
      .fetchSet(0, String::class.java)
      .sortedBy { it.toLowerCase() }
      .toSet()

  override fun findAllTagsByLibrary(libraryId: String): Set<String> =
    dsl.select(st.TAG)
      .from(st)
      .leftJoin(s).on(st.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.eq(libraryId))
      .orderBy(lower(st.TAG))
      .fetchSet(st.TAG)

  override fun findAllTagsBySeries(seriesId: String): Set<String> =
    dsl.select(bt.TAG)
      .from(bt)
      .leftJoin(b).on(bt.BOOK_ID.eq(b.ID))
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(lower(bt.TAG))
      .fetchSet(bt.TAG)

  override fun findAllTagsByCollection(collectionId: String): Set<String> =
    dsl.select(st.TAG)
      .from(st)
      .leftJoin(cs).on(st.SERIES_ID.eq(cs.SERIES_ID))
      .where(cs.COLLECTION_ID.eq(collectionId))
      .orderBy(lower(st.TAG))
      .fetchSet(st.TAG)

  override fun findAllLanguages(): Set<String> =
    dsl.selectDistinct(sd.LANGUAGE)
      .from(sd)
      .where(sd.LANGUAGE.ne(""))
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)

  override fun findAllLanguagesByLibrary(libraryId: String): Set<String> =
    dsl.selectDistinct(sd.LANGUAGE)
      .from(sd)
      .leftJoin(s).on(sd.SERIES_ID.eq(s.ID))
      .where(sd.LANGUAGE.ne(""))
      .and(s.LIBRARY_ID.eq(libraryId))
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)

  override fun findAllLanguagesByCollection(collectionId: String): Set<String> =
    dsl.selectDistinct(sd.LANGUAGE)
      .from(sd)
      .leftJoin(cs).on(sd.SERIES_ID.eq(cs.SERIES_ID))
      .where(sd.LANGUAGE.ne(""))
      .and(cs.COLLECTION_ID.eq(collectionId))
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)

  override fun findAllPublishers(): Set<String> =
    dsl.selectDistinct(sd.PUBLISHER)
      .from(sd)
      .where(sd.PUBLISHER.ne(""))
      .orderBy(sd.PUBLISHER)
      .fetchSet(sd.PUBLISHER)

  override fun findAllPublishersByLibrary(libraryId: String): Set<String> =
    dsl.selectDistinct(sd.PUBLISHER)
      .from(sd)
      .leftJoin(s).on(sd.SERIES_ID.eq(s.ID))
      .where(sd.PUBLISHER.ne(""))
      .and(s.LIBRARY_ID.eq(libraryId))
      .orderBy(sd.PUBLISHER)
      .fetchSet(sd.PUBLISHER)

  override fun findAllPublishersByLibraries(libraryIds: Set<String>): Set<String> =
    dsl.selectDistinct(sd.PUBLISHER)
      .from(sd)
      .leftJoin(s).on(sd.SERIES_ID.eq(s.ID))
      .where(sd.PUBLISHER.ne(""))
      .and(s.LIBRARY_ID.`in`(libraryIds))
      .orderBy(sd.PUBLISHER)
      .fetchSet(sd.PUBLISHER)

  override fun findAllPublishersByCollection(collectionId: String): Set<String> =
    dsl.selectDistinct(sd.PUBLISHER)
      .from(sd)
      .leftJoin(cs).on(sd.SERIES_ID.eq(cs.SERIES_ID))
      .where(sd.PUBLISHER.ne(""))
      .and(cs.COLLECTION_ID.eq(collectionId))
      .orderBy(sd.PUBLISHER)
      .fetchSet(sd.PUBLISHER)

  override fun findAllAgeRatings(): Set<Int> =
    dsl.selectDistinct(sd.AGE_RATING)
      .from(sd)
      .orderBy(sd.AGE_RATING)
      .fetchSet(sd.AGE_RATING)

  override fun findAllAgeRatingsByLibrary(libraryId: String): Set<Int> =
    dsl.selectDistinct(sd.AGE_RATING)
      .from(sd)
      .leftJoin(s).on(sd.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.eq(libraryId))
      .orderBy(sd.AGE_RATING)
      .fetchSet(sd.AGE_RATING)

  override fun findAllAgeRatingsByCollection(collectionId: String): Set<Int?> =
    dsl.selectDistinct(sd.AGE_RATING)
      .from(sd)
      .leftJoin(cs).on(sd.SERIES_ID.eq(cs.SERIES_ID))
      .where(cs.COLLECTION_ID.eq(collectionId))
      .orderBy(sd.AGE_RATING)
      .fetchSet(sd.AGE_RATING)

  override fun findAllSeriesReleaseDates(): Set<LocalDate> =
    dsl.selectDistinct(bma.RELEASE_DATE)
      .from(bma)
      .where(bma.RELEASE_DATE.isNotNull)
      .orderBy(bma.RELEASE_DATE.desc())
      .fetchSet(bma.RELEASE_DATE)

  override fun findAllSeriesReleaseDatesByLibrary(libraryId: String): Set<LocalDate> =
    dsl.selectDistinct(bma.RELEASE_DATE)
      .from(bma)
      .leftJoin(s).on(bma.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.eq(libraryId))
      .and(bma.RELEASE_DATE.isNotNull)
      .orderBy(bma.RELEASE_DATE.desc())
      .fetchSet(bma.RELEASE_DATE)

  override fun findAllSeriesReleaseDatesByCollection(collectionId: String): Set<LocalDate> =
    dsl.selectDistinct(bma.RELEASE_DATE)
      .from(bma)
      .leftJoin(cs).on(bma.SERIES_ID.eq(cs.SERIES_ID))
      .where(cs.COLLECTION_ID.eq(collectionId))
      .and(bma.RELEASE_DATE.isNotNull)
      .orderBy(bma.RELEASE_DATE.desc())
      .fetchSet(bma.RELEASE_DATE)

  private fun BookMetadataAuthorRecord.toDomain(): Author =
    Author(
      name = name,
      role = role
    )

  private fun BookMetadataAggregationAuthorRecord.toDomain(): Author =
    Author(
      name = name,
      role = role
    )
}
