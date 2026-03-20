package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.FilterBy
import org.gotson.komga.domain.model.FilterByEntity
import org.gotson.komga.domain.model.FilterTags
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.infrastructure.jooq.ContentRestrictionsSearchHelper
import org.gotson.komga.infrastructure.jooq.RequiredJoin
import org.gotson.komga.infrastructure.jooq.SplitDslDaoBase
import org.gotson.komga.infrastructure.jooq.buildPage
import org.gotson.komga.infrastructure.jooq.udfStripAccents
import org.gotson.komga.infrastructure.jooq.unicode3
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookMetadataAggregationAuthorRecord
import org.gotson.komga.jooq.main.tables.records.BookMetadataAuthorRecord
import org.gotson.komga.language.stripAccents
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.OrderField
import org.jooq.SelectFieldOrAsterisk
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.impl.DSL.select
import org.jooq.impl.TableImpl
import org.jooq.impl.TableRecordImpl
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ReferentialDao(
  dslRW: DSLContext,
  @Qualifier("dslContextRO") dslRO: DSLContext,
) : SplitDslDaoBase(dslRW, dslRO),
  ReferentialRepository {
  private val a = Tables.BOOK_METADATA_AUTHOR
  private val sd = Tables.SERIES_METADATA
  private val bma = Tables.BOOK_METADATA_AGGREGATION
  private val bmaa = Tables.BOOK_METADATA_AGGREGATION_AUTHOR
  private val bmat = Tables.BOOK_METADATA_AGGREGATION_TAG
  private val s = Tables.SERIES
  private val b = Tables.BOOK
  private val g = Tables.SERIES_METADATA_GENRE
  private val bt = Tables.BOOK_METADATA_TAG
  private val st = Tables.SERIES_METADATA_TAG
  private val cs = Tables.COLLECTION_SERIES
  private val rb = Tables.READLIST_BOOK
  private val sl = Tables.SERIES_METADATA_SHARING
  private val at = Tables.SERIES_AND_BOOK_TAG

  @Deprecated("Use findAuthors instead")
  override fun findAllAuthorsByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author> =
    dslRO
      .selectDistinct(a.NAME, a.ROLE)
      .from(a)
      .apply { filterOnLibraryIds?.let { leftJoin(b).on(a.BOOK_ID.eq(b.ID)) } }
      .where(a.NAME.udfStripAccents().contains(search.stripAccents()))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .orderBy(a.NAME.unicode3())
      .fetchInto(a)
      .map { it.toDomain() }

  @Deprecated("Use findAuthors instead")
  override fun findAllAuthorsByNameAndLibrary(
    search: String,
    libraryId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author> =
    dslRO
      .selectDistinct(bmaa.NAME, bmaa.ROLE)
      .from(bmaa)
      .leftJoin(s)
      .on(bmaa.SERIES_ID.eq(s.ID))
      .where(bmaa.NAME.udfStripAccents().contains(search.stripAccents()))
      .and(s.LIBRARY_ID.eq(libraryId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bmaa.NAME.unicode3())
      .fetchInto(bmaa)
      .map { it.toDomain() }

  @Deprecated("Use findAuthors instead")
  override fun findAllAuthorsByNameAndCollection(
    search: String,
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author> =
    dslRO
      .selectDistinct(bmaa.NAME, bmaa.ROLE)
      .from(bmaa)
      .leftJoin(cs)
      .on(bmaa.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(bmaa.SERIES_ID.eq(s.ID)) } }
      .where(bmaa.NAME.udfStripAccents().contains(search.stripAccents()))
      .and(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bmaa.NAME.unicode3())
      .fetchInto(bmaa)
      .map { it.toDomain() }

  @Deprecated("Use findAuthors instead")
  override fun findAllAuthorsByNameAndSeries(
    search: String,
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author> =
    dslRO
      .selectDistinct(bmaa.NAME, bmaa.ROLE)
      .from(bmaa)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(bmaa.SERIES_ID.eq(s.ID)) } }
      .where(bmaa.NAME.udfStripAccents().contains(search.stripAccents()))
      .and(bmaa.SERIES_ID.eq(seriesId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bmaa.NAME.unicode3())
      .fetchInto(bmaa)
      .map { it.toDomain() }

  override fun findAuthors(
    context: SearchContext,
    search: String?,
    role: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<Author> = findGeneric(context, search, filterBy, pageable, bmaa, bmaa.NAME, bmaa.SERIES_ID, { it?.toDomain() }, Sort.by("name"), listOf(bmaa.ROLE), role?.let { bmaa.ROLE.eq(role) })

  override fun findAuthorsRoles(
    context: SearchContext,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String> = findGeneric(context, null, filterBy, pageable, bmaa, null, bmaa.SERIES_ID, { it?.role }, Sort.by("role"), listOf(bmaa.ROLE), sortField = bmaa.ROLE)

  override fun findAuthorsNames(
    context: SearchContext,
    search: String?,
    role: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String> = findGeneric(context, search, filterBy, pageable, bmaa, bmaa.NAME, bmaa.SERIES_ID, { it?.name }, Sort.by("name"), listOf(bmaa.ROLE), role?.let { bmaa.ROLE.eq(role) })

  @Deprecated("Use findAuthorsNames instead")
  override fun findAllAuthorsNamesByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<String> =
    dslRO
      .selectDistinct(a.NAME)
      .from(a)
      .apply { filterOnLibraryIds?.let { leftJoin(b).on(a.BOOK_ID.eq(b.ID)) } }
      .where(a.NAME.udfStripAccents().contains(search.stripAccents()))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .orderBy(a.NAME.unicode3())
      .fetch(a.NAME)

  @Deprecated("Use findAuthorsRoles instead")
  override fun findAllAuthorsRoles(filterOnLibraryIds: Collection<String>?): List<String> =
    dslRO
      .selectDistinct(a.ROLE)
      .from(a)
      .apply {
        filterOnLibraryIds?.let {
          leftJoin(b)
            .on(a.BOOK_ID.eq(b.ID))
            .where(b.LIBRARY_ID.`in`(it))
        }
      }.orderBy(a.ROLE)
      .fetch(a.ROLE)

  @Deprecated("Use findGenres instead")
  override fun findAllGenres(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .selectDistinct(g.GENRE)
      .from(g)
      .apply {
        filterOnLibraryIds?.let {
          leftJoin(s)
            .on(g.SERIES_ID.eq(s.ID))
            .where(s.LIBRARY_ID.`in`(it))
        }
      }.orderBy(g.GENRE.unicode3())
      .fetchSet(g.GENRE)

  @Deprecated("Use findGenres instead")
  override fun findAllGenresByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(g.GENRE)
      .from(g)
      .leftJoin(s)
      .on(g.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.`in`(libraryIds))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(g.GENRE.unicode3())
      .fetchSet(g.GENRE)

  @Deprecated("Use findGenres instead")
  override fun findAllGenresByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(g.GENRE)
      .from(g)
      .leftJoin(cs)
      .on(g.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(g.SERIES_ID.eq(s.ID)) } }
      .where(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(g.GENRE.unicode3())
      .fetchSet(g.GENRE)

  override fun findGenres(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String> {
    filterBy?.let { require(it.type in setOf(FilterByEntity.LIBRARY, FilterByEntity.COLLECTION)) }

    return findGeneric(context, search, filterBy, pageable, g, g.GENRE, g.SERIES_ID, { it?.genre }, Sort.by("genre"))
  }

  @Deprecated("Use findTags instead")
  override fun findAllSeriesAndBookTags(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .select(bt.TAG.`as`("tag"))
      .from(bt)
      .apply { filterOnLibraryIds?.let { leftJoin(b).on(bt.BOOK_ID.eq(b.ID)).where(b.LIBRARY_ID.`in`(it)) } }
      .union(
        select(st.TAG.`as`("tag"))
          .from(st)
          .apply { filterOnLibraryIds?.let { leftJoin(s).on(st.SERIES_ID.eq(s.ID)).where(s.LIBRARY_ID.`in`(it)) } },
      ).fetchSet(0, String::class.java)
      .sortedBy { it.stripAccents().lowercase() }
      .toSet()

  @Deprecated("Use findTags instead")
  override fun findAllSeriesAndBookTagsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(bt.TAG.`as`("tag"))
      .from(bt)
      .leftJoin(b)
      .on(bt.BOOK_ID.eq(b.ID))
      .where(b.LIBRARY_ID.`in`(libraryIds))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .union(
        select(st.TAG.`as`("tag"))
          .from(st)
          .leftJoin(s)
          .on(st.SERIES_ID.eq(s.ID))
          .where(s.LIBRARY_ID.`in`(libraryIds))
          .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } },
      ).fetchSet(0, String::class.java)
      .sortedBy { it.stripAccents().lowercase() }
      .toSet()

  @Deprecated("Use findTags instead")
  override fun findAllSeriesAndBookTagsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(bmat.TAG.`as`("tag"))
      .from(bmat)
      .leftJoin(s)
      .on(bmat.SERIES_ID.eq(s.ID))
      .leftJoin(cs)
      .on(bmat.SERIES_ID.eq(cs.SERIES_ID))
      .where(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .union(
        select(st.TAG.`as`("tag"))
          .from(st)
          .leftJoin(cs)
          .on(st.SERIES_ID.eq(cs.SERIES_ID))
          .leftJoin(s)
          .on(st.SERIES_ID.eq(s.ID))
          .where(cs.COLLECTION_ID.eq(collectionId))
          .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } },
      ).fetchSet(0, String::class.java)
      .sortedBy { it.stripAccents().lowercase() }
      .toSet()

  @Deprecated("Use findTags instead")
  override fun findAllSeriesTags(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .select(st.TAG)
      .from(st)
      .apply {
        filterOnLibraryIds?.let {
          leftJoin(s)
            .on(st.SERIES_ID.eq(s.ID))
            .where(s.LIBRARY_ID.`in`(it))
        }
      }.orderBy(st.TAG.unicode3())
      .fetchSet(st.TAG)

  @Deprecated("Use findTags instead")
  override fun findAllSeriesTagsByLibrary(
    libraryId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(st.TAG)
      .from(st)
      .leftJoin(s)
      .on(st.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.eq(libraryId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(st.TAG.unicode3())
      .fetchSet(st.TAG)

  @Deprecated("Use findTags instead")
  override fun findAllBookTagsBySeries(
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(bt.TAG)
      .from(bt)
      .leftJoin(b)
      .on(bt.BOOK_ID.eq(b.ID))
      .where(b.SERIES_ID.eq(seriesId))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .orderBy(bt.TAG.unicode3())
      .fetchSet(bt.TAG)

  @Deprecated("Use findTags instead")
  override fun findAllBookTagsByReadList(
    readListId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(bt.TAG)
      .from(bt)
      .leftJoin(b)
      .on(bt.BOOK_ID.eq(b.ID))
      .leftJoin(rb)
      .on(bt.BOOK_ID.eq(rb.BOOK_ID))
      .where(rb.READLIST_ID.eq(readListId))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .orderBy(bt.TAG.unicode3())
      .fetchSet(bt.TAG)

  override fun findTags(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    filterTags: FilterTags,
    pageable: Pageable,
  ): Page<String> =
    when (filterTags) {
      FilterTags.SERIES -> findGeneric(context, search, filterBy, pageable, st, st.TAG, st.SERIES_ID, { it?.tag }, Sort.by("tag"))
      FilterTags.BOOK -> findGeneric(context, search, filterBy, pageable, bmat, bmat.TAG, bmat.SERIES_ID, { it?.tag }, Sort.by("tag"))
      FilterTags.BOTH -> findGeneric(context, search, filterBy, pageable, at, at.TAG, at.SERIES_ID, { it?.tag }, Sort.by("tag"))
    }

  @Deprecated("Use findTags instead")
  override fun findAllSeriesTagsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(st.TAG)
      .from(st)
      .leftJoin(cs)
      .on(st.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(st.SERIES_ID.eq(s.ID)) } }
      .where(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(st.TAG.unicode3())
      .fetchSet(st.TAG)

  @Deprecated("Use findTags instead")
  override fun findAllBookTags(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .select(bt.TAG)
      .from(bt)
      .apply {
        filterOnLibraryIds?.let {
          leftJoin(b)
            .on(bt.BOOK_ID.eq(b.ID))
            .where(b.LIBRARY_ID.`in`(it))
        }
      }.orderBy(bt.TAG.unicode3())
      .fetchSet(bt.TAG)

  @Deprecated("Use findLanguages instead")
  override fun findAllLanguages(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .selectDistinct(sd.LANGUAGE)
      .from(sd)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
      .where(sd.LANGUAGE.ne(""))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)

  @Deprecated("Use findLanguages instead")
  override fun findAllLanguagesByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(sd.LANGUAGE)
      .from(sd)
      .leftJoin(s)
      .on(sd.SERIES_ID.eq(s.ID))
      .where(sd.LANGUAGE.ne(""))
      .and(s.LIBRARY_ID.`in`(libraryIds))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)

  @Deprecated("Use findLanguages instead")
  override fun findAllLanguagesByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(sd.LANGUAGE)
      .from(sd)
      .leftJoin(cs)
      .on(sd.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
      .where(sd.LANGUAGE.ne(""))
      .and(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)

  override fun findLanguages(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String> {
    filterBy?.let { require(it.type in setOf(FilterByEntity.LIBRARY, FilterByEntity.COLLECTION)) }

    return findGeneric(context, search, filterBy, pageable, sd, sd.LANGUAGE, sd.SERIES_ID, { it?.language }, Sort.by("language"), extraCondition = sd.LANGUAGE.ne(""))
  }

  @Deprecated("Use findPublishers instead")
  override fun findAllPublishers(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .selectDistinct(sd.PUBLISHER)
      .from(sd)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
      .where(sd.PUBLISHER.ne(""))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.PUBLISHER.unicode3())
      .fetchSet(sd.PUBLISHER)

  @Deprecated("Use findPublishers instead")
  override fun findAllPublishers(
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<String> {
    val query =
      dslRO
        .selectDistinct(sd.PUBLISHER)
        .from(sd)
        .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
        .where(sd.PUBLISHER.ne(""))
        .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }

    val count = dslRO.fetchCount(query)
    val sort = sd.PUBLISHER.unicode3()

    val items =
      query
        .orderBy(sort)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetch(sd.PUBLISHER)

    val pageSort = Sort.by("name")
    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  @Deprecated("Use findPublishers instead")
  override fun findAllPublishersByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(sd.PUBLISHER)
      .from(sd)
      .leftJoin(s)
      .on(sd.SERIES_ID.eq(s.ID))
      .where(sd.PUBLISHER.ne(""))
      .and(s.LIBRARY_ID.`in`(libraryIds))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.PUBLISHER.unicode3())
      .fetchSet(sd.PUBLISHER)

  @Deprecated("Use findPublishers instead")
  override fun findAllPublishersByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(sd.PUBLISHER)
      .from(sd)
      .leftJoin(cs)
      .on(sd.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
      .where(sd.PUBLISHER.ne(""))
      .and(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.PUBLISHER.unicode3())
      .fetchSet(sd.PUBLISHER)

  override fun findPublishers(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String> {
    filterBy?.let { require(it.type in setOf(FilterByEntity.LIBRARY, FilterByEntity.COLLECTION)) }

    return findGeneric(context, search, filterBy, pageable, sd, sd.PUBLISHER, sd.SERIES_ID, { it?.publisher }, Sort.by("publisher"), extraCondition = sd.PUBLISHER.ne(""))
  }

  @Deprecated("Use findAgeRatings instead")
  override fun findAllAgeRatings(filterOnLibraryIds: Collection<String>?): Set<Int?> =
    dslRO
      .selectDistinct(sd.AGE_RATING)
      .from(sd)
      .apply {
        filterOnLibraryIds?.let {
          leftJoin(s)
            .on(sd.SERIES_ID.eq(s.ID))
            .where(s.LIBRARY_ID.`in`(it))
        }
      }.orderBy(sd.AGE_RATING)
      .fetchSet(sd.AGE_RATING)

  @Deprecated("Use findAgeRatings instead")
  override fun findAllAgeRatingsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<Int?> =
    dslRO
      .selectDistinct(sd.AGE_RATING)
      .from(sd)
      .leftJoin(s)
      .on(sd.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.`in`(libraryIds))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.AGE_RATING)
      .fetchSet(sd.AGE_RATING)

  @Deprecated("Use findAgeRatings instead")
  override fun findAllAgeRatingsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<Int?> =
    dslRO
      .selectDistinct(sd.AGE_RATING)
      .from(sd)
      .leftJoin(cs)
      .on(sd.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
      .where(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.AGE_RATING)
      .fetchSet(sd.AGE_RATING)

  override fun findAgeRatings(
    context: SearchContext,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<Int> {
    filterBy?.let { require(it.type in setOf(FilterByEntity.LIBRARY, FilterByEntity.COLLECTION)) }

    return findGeneric(context, null, filterBy, pageable, sd, null, sd.SERIES_ID, { it?.ageRating }, Sort.by("ageRating"), listOf(sd.AGE_RATING), sortField = sd.AGE_RATING)
  }

  @Deprecated("Use findSeriesReleaseDates instead")
  override fun findAllSeriesReleaseDates(filterOnLibraryIds: Collection<String>?): Set<LocalDate> =
    dslRO
      .selectDistinct(bma.RELEASE_DATE)
      .from(bma)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(bma.SERIES_ID.eq(s.ID)) } }
      .where(bma.RELEASE_DATE.isNotNull)
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bma.RELEASE_DATE.desc())
      .fetchSet(bma.RELEASE_DATE)

  @Deprecated("Use findSeriesReleaseDates instead")
  override fun findAllSeriesReleaseDatesByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<LocalDate> =
    dslRO
      .selectDistinct(bma.RELEASE_DATE)
      .from(bma)
      .leftJoin(s)
      .on(bma.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.`in`(libraryIds))
      .and(bma.RELEASE_DATE.isNotNull)
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bma.RELEASE_DATE.desc())
      .fetchSet(bma.RELEASE_DATE)

  @Deprecated("Use findSeriesReleaseDates instead")
  override fun findAllSeriesReleaseDatesByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<LocalDate> =
    dslRO
      .selectDistinct(bma.RELEASE_DATE)
      .from(bma)
      .leftJoin(cs)
      .on(bma.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(bma.SERIES_ID.eq(s.ID)) } }
      .where(cs.COLLECTION_ID.eq(collectionId))
      .and(bma.RELEASE_DATE.isNotNull)
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bma.RELEASE_DATE.desc())
      .fetchSet(bma.RELEASE_DATE)

  override fun findSeriesReleaseYears(
    context: SearchContext,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String> {
    filterBy?.let { require(it.type in setOf(FilterByEntity.LIBRARY, FilterByEntity.COLLECTION)) }

    val sortField = bma.RELEASE_DATE.desc()
    val restrictionCondition = ContentRestrictionsSearchHelper(context.restrictions).toCondition()
    val query =
      dslRO
        .selectDistinct(DSL.year(bma.RELEASE_DATE))
        .from(bma)
        .apply {
          restrictionCondition.second.forEach { join ->
            when (join) {
              RequiredJoin.SeriesMetadata -> innerJoin(sd).on(bma.SERIES_ID.eq(sd.SERIES_ID))
              // shouldn't be required
              RequiredJoin.BookMetadata -> Unit
              RequiredJoin.BookMetadataAggregation -> Unit
              is RequiredJoin.Collection -> Unit
              RequiredJoin.Media -> Unit
              is RequiredJoin.ReadList -> Unit
              is RequiredJoin.ReadProgress -> Unit
            }
          }
        }.apply { if (!context.libraryIds.isNullOrEmpty() || filterBy?.type == FilterByEntity.LIBRARY) leftJoin(s).on(bma.SERIES_ID.eq(s.ID)) }
        .apply { if (filterBy?.type == FilterByEntity.COLLECTION) leftJoin(cs).on(bma.SERIES_ID.eq(cs.SERIES_ID)) }
        .apply {
          if (filterBy?.type == FilterByEntity.READLIST)
            leftJoin(b)
              .on(bma.SERIES_ID.eq(b.SERIES_ID))
              .leftJoin(rb)
              .on(b.ID.eq(rb.BOOK_ID))
        }.where(restrictionCondition.first)
        .apply { context.libraryIds?.let { this.and(s.LIBRARY_ID.`in`(it)) } }
        .apply {
          filterBy?.let {
            when (it.type) {
              FilterByEntity.LIBRARY -> this.and(s.LIBRARY_ID.`in`(it.ids))
              FilterByEntity.COLLECTION -> this.and(cs.COLLECTION_ID.`in`(it.ids))
              FilterByEntity.SERIES -> this.and(bma.SERIES_ID.`in`(it.ids))
              FilterByEntity.READLIST -> this.and(rb.READLIST_ID.`in`(it.ids))
            }
          }
        }
    val count = dslRO.fetchCount(query)
    val items =
      query
        .orderBy(sortField)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchArray(0)
        .mapNotNull { it?.toString() }
    return buildPage(items, pageable, count, Sort.by(Order.desc("year")))
  }

  @Deprecated("Use findSharingLabels instead")
  override fun findAllSharingLabels(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .selectDistinct(sl.LABEL)
      .from(sl)
      .apply {
        filterOnLibraryIds?.let {
          leftJoin(s)
            .on(sl.SERIES_ID.eq(s.ID))
            .where(s.LIBRARY_ID.`in`(it))
        }
      }.orderBy(sl.LABEL.unicode3())
      .fetchSet(sl.LABEL)

  @Deprecated("Use findSharingLabels instead")
  override fun findAllSharingLabelsByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(sl.LABEL)
      .from(sl)
      .leftJoin(s)
      .on(sl.SERIES_ID.eq(s.ID))
      .where(s.LIBRARY_ID.`in`(libraryIds))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sl.LABEL.unicode3())
      .fetchSet(sl.LABEL)

  @Deprecated("Use findSharingLabels instead")
  override fun findAllSharingLabelsByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .selectDistinct(sl.LABEL)
      .from(sl)
      .leftJoin(cs)
      .on(sl.SERIES_ID.eq(cs.SERIES_ID))
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sl.SERIES_ID.eq(s.ID)) } }
      .where(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sl.LABEL.unicode3())
      .fetchSet(sl.LABEL)

  override fun findSharingLabels(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
  ): Page<String> {
    filterBy?.let { require(it.type in setOf(FilterByEntity.LIBRARY, FilterByEntity.COLLECTION)) }

    return findGeneric(context, search, filterBy, pageable, sl, sl.LABEL, sl.SERIES_ID, { it?.label }, Sort.by("label"))
  }

  private fun <R : TableRecordImpl<*>, T : TableImpl<R>, O : Any> findGeneric(
    context: SearchContext,
    search: String?,
    filterBy: FilterBy?,
    pageable: Pageable,
    table: T,
    searchableField: TableField<R, String>?,
    seriesIdField: TableField<*, String>,
    mapper: (R?) -> O?,
    sort: Sort,
    extraFields: List<SelectFieldOrAsterisk> = emptyList(),
    extraCondition: Condition? = DSL.noCondition(),
    sortField: OrderField<*>? = null,
  ): Page<O> {
    val restrictionCondition = ContentRestrictionsSearchHelper(context.restrictions).toCondition()

    val query =
      dslRO
        .selectDistinct(*(listOfNotNull(searchableField) + extraFields).toTypedArray())
        .from(table)
        .apply {
          restrictionCondition.second.forEach { join ->
            when (join) {
              RequiredJoin.SeriesMetadata -> if (table != sd) innerJoin(sd).on(seriesIdField.eq(sd.SERIES_ID))
              // shouldn't be required
              RequiredJoin.BookMetadata -> Unit
              RequiredJoin.BookMetadataAggregation -> Unit
              is RequiredJoin.Collection -> Unit
              RequiredJoin.Media -> Unit
              is RequiredJoin.ReadList -> Unit
              is RequiredJoin.ReadProgress -> Unit
            }
          }
        }.apply { if (!context.libraryIds.isNullOrEmpty() || filterBy?.type == FilterByEntity.LIBRARY) leftJoin(s).on(seriesIdField.eq(s.ID)) }
        .apply { if (filterBy?.type == FilterByEntity.COLLECTION) leftJoin(cs).on(seriesIdField.eq(cs.SERIES_ID)) }
        .apply {
          if (filterBy?.type == FilterByEntity.READLIST)
            leftJoin(b)
              .on(seriesIdField.eq(b.SERIES_ID))
              .leftJoin(rb)
              .on(b.ID.eq(rb.BOOK_ID))
        }.where(restrictionCondition.first)
        .apply { extraCondition?.let { and(it) } }
        .apply { if (search != null && searchableField != null) and(searchableField.udfStripAccents().contains(search.stripAccents())) }
        .apply { context.libraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
        .apply {
          filterBy?.let {
            when (it.type) {
              FilterByEntity.LIBRARY -> and(s.LIBRARY_ID.`in`(it.ids))
              FilterByEntity.COLLECTION -> and(cs.COLLECTION_ID.`in`(it.ids))
              FilterByEntity.SERIES -> and(seriesIdField.`in`(it.ids))
              FilterByEntity.READLIST -> and(rb.READLIST_ID.`in`(it.ids))
            }
          }
        }

    val count = dslRO.fetchCount(query)

    val items =
      query
        .apply {
          if (sortField != null)
            orderBy(sortField)
          else if (searchableField != null)
            orderBy(searchableField.unicode3())
        }.apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(table)
        .mapNotNull { mapper(it) }

    return buildPage(items, pageable, count, sort)
  }

  private fun BookMetadataAuthorRecord.toDomain(): Author =
    Author(
      name = name,
      role = role,
    )

  private fun BookMetadataAggregationAuthorRecord.toDomain(): Author =
    Author(
      name = name,
      role = role,
    )
}
