package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.jooq.Tables
import org.jooq.DSLContext
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component

@Component
class ReferentialDao(
  private val dsl: DSLContext
) : ReferentialRepository {

  private val a = Tables.BOOK_METADATA_AUTHOR
  private val sd = Tables.SERIES_METADATA
  private val s = Tables.SERIES
  private val b = Tables.BOOK
  private val g = Tables.SERIES_METADATA_GENRE
  private val bt = Tables.BOOK_METADATA_TAG
  private val st = Tables.SERIES_METADATA_TAG

  override fun findAuthorsByName(search: String): List<String> =
    dsl.selectDistinct(a.NAME)
      .from(a)
      .where(a.NAME.containsIgnoreCase(search))
      .orderBy(a.NAME)
      .fetch(a.NAME)

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
}
