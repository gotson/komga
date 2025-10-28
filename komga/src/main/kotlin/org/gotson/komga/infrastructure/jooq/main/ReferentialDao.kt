package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.SplitDslDaoBase
import org.gotson.komga.infrastructure.jooq.udfStripAccents
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookMetadataAggregationAuthorRecord
import org.gotson.komga.jooq.main.tables.records.BookMetadataAuthorRecord
import org.gotson.komga.language.stripAccents
import org.jooq.DSLContext
import org.jooq.impl.DSL.noCondition
import org.jooq.impl.DSL.select
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
  private val bc = Tables.BOOK_METADATA_CHARACTER
  private val cs = Tables.COLLECTION_SERIES
  private val rb = Tables.READLIST_BOOK
  private val sl = Tables.SERIES_METADATA_SHARING

  override fun findAllAuthorsByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author> =
    dslRO
      .selectDistinct(a.NAME, a.ROLE)
      .from(a)
      .apply { filterOnLibraryIds?.let { leftJoin(b).on(a.BOOK_ID.eq(b.ID)) } }
      .where(a.NAME.udfStripAccents().containsIgnoreCase(search.stripAccents()))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .orderBy(a.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchInto(a)
      .map { it.toDomain() }

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
      .where(bmaa.NAME.udfStripAccents().containsIgnoreCase(search.stripAccents()))
      .and(s.LIBRARY_ID.eq(libraryId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bmaa.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchInto(bmaa)
      .map { it.toDomain() }

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
      .where(bmaa.NAME.udfStripAccents().containsIgnoreCase(search.stripAccents()))
      .and(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bmaa.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchInto(bmaa)
      .map { it.toDomain() }

  override fun findAllAuthorsByNameAndSeries(
    search: String,
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<Author> =
    dslRO
      .selectDistinct(bmaa.NAME, bmaa.ROLE)
      .from(bmaa)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(bmaa.SERIES_ID.eq(s.ID)) } }
      .where(bmaa.NAME.udfStripAccents().containsIgnoreCase(search.stripAccents()))
      .and(bmaa.SERIES_ID.eq(seriesId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bmaa.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchInto(bmaa)
      .map { it.toDomain() }

  override fun findAllAuthorsByName(
    search: String?,
    role: String?,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author> = findAuthorsByName(search, role, filterOnLibraryIds, pageable, null)

  override fun findAllAuthorsByNameAndLibraries(
    search: String?,
    role: String?,
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author> = findAuthorsByName(search, role, filterOnLibraryIds, pageable, FilterBy(FilterByType.LIBRARY, libraryIds))

  override fun findAllAuthorsByNameAndCollection(
    search: String?,
    role: String?,
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author> = findAuthorsByName(search, role, filterOnLibraryIds, pageable, FilterBy(FilterByType.COLLECTION, setOf(collectionId)))

  override fun findAllAuthorsByNameAndSeries(
    search: String?,
    role: String?,
    seriesId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author> = findAuthorsByName(search, role, filterOnLibraryIds, pageable, FilterBy(FilterByType.SERIES, setOf(seriesId)))

  override fun findAllAuthorsByNameAndReadList(
    search: String?,
    role: String?,
    readListId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
  ): Page<Author> = findAuthorsByName(search, role, filterOnLibraryIds, pageable, FilterBy(FilterByType.READLIST, setOf(readListId)))

  private enum class FilterByType {
    LIBRARY,
    COLLECTION,
    SERIES,
    READLIST,
  }

  private data class FilterBy(
    val type: FilterByType,
    val ids: Set<String>,
  )

  private fun findAuthorsByName(
    search: String?,
    role: String?,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
    filterBy: FilterBy?,
  ): Page<Author> {
    val query =
      dslRO
        .selectDistinct(bmaa.NAME, bmaa.ROLE)
        .from(bmaa)
        .apply { if (filterOnLibraryIds != null || filterBy?.type == FilterByType.LIBRARY) leftJoin(s).on(bmaa.SERIES_ID.eq(s.ID)) }
        .apply { if (filterBy?.type == FilterByType.COLLECTION) leftJoin(cs).on(bmaa.SERIES_ID.eq(cs.SERIES_ID)) }
        .apply {
          if (filterBy?.type == FilterByType.READLIST)
            leftJoin(b)
              .on(bmaa.SERIES_ID.eq(b.SERIES_ID))
              .leftJoin(rb)
              .on(b.ID.eq(rb.BOOK_ID))
        }.where(noCondition())
        .apply { search?.let { and(bmaa.NAME.udfStripAccents().containsIgnoreCase(search.stripAccents())) } }
        .apply { role?.let { and(bmaa.ROLE.eq(role)) } }
        .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
        .apply {
          filterBy?.let {
            when (it.type) {
              FilterByType.LIBRARY -> and(s.LIBRARY_ID.`in`(it.ids))
              FilterByType.COLLECTION -> and(cs.COLLECTION_ID.`in`(it.ids))
              FilterByType.SERIES -> and(bmaa.SERIES_ID.`in`(it.ids))
              FilterByType.READLIST -> and(rb.READLIST_ID.`in`(it.ids))
            }
          }
        }

    val count = dslRO.fetchCount(query)
    val sort = bmaa.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3)

    val items =
      query
        .orderBy(sort)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(a)
        .map { it.toDomain() }

    val pageSort = Sort.by("relevance")
    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findAllAuthorsNamesByName(
    search: String,
    filterOnLibraryIds: Collection<String>?,
  ): List<String> =
    dslRO
      .selectDistinct(a.NAME)
      .from(a)
      .apply { filterOnLibraryIds?.let { leftJoin(b).on(a.BOOK_ID.eq(b.ID)) } }
      .where(a.NAME.udfStripAccents().containsIgnoreCase(search.stripAccents()))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .orderBy(a.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetch(a.NAME)

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
      }.orderBy(g.GENRE.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(g.GENRE)

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
      .orderBy(g.GENRE.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(g.GENRE)

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
      .orderBy(g.GENRE.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(g.GENRE)

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

  override fun findAllSeriesAndBookCharacters(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .select(bc.CHARACTER.`as`("character"))
      .from(bc)
      .apply { filterOnLibraryIds?.let { leftJoin(b).on(bc.BOOK_ID.eq(b.ID)).where(b.LIBRARY_ID.`in`(it)) } }
      .fetchSet(0, String::class.java)
      .sortedBy { it.stripAccents().lowercase() }
      .toSet()

  override fun findAllSeriesAndBookCharactersByLibraries(
    libraryIds: Set<String>,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(bc.CHARACTER.`as`("character"))
      .from(bc)
      .leftJoin(b)
      .on(bc.BOOK_ID.eq(b.ID))
      .where(b.LIBRARY_ID.`in`(libraryIds))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .fetchSet(0, String::class.java)
      .sortedBy { it.stripAccents().lowercase() }
      .toSet()

  override fun findAllSeriesAndBookCharactersByCollection(
    collectionId: String,
    filterOnLibraryIds: Collection<String>?,
  ): Set<String> =
    dslRO
      .select(bc.CHARACTER.`as`("character"))
      .from(bc)
      .leftJoin(b)
      .on(bc.BOOK_ID.eq(b.ID))
      .leftJoin(s)
      .on(b.SERIES_ID.eq(s.ID))
      .leftJoin(cs)
      .on(s.ID.eq(cs.SERIES_ID))
      .where(cs.COLLECTION_ID.eq(collectionId))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .fetchSet(0, String::class.java)
      .sortedBy { it.stripAccents().lowercase() }
      .toSet()

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
      }.orderBy(st.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(st.TAG)

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
      .orderBy(st.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(st.TAG)

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
      .orderBy(bt.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(bt.TAG)

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
      .orderBy(bt.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(bt.TAG)

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
      .orderBy(st.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(st.TAG)

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
      }.orderBy(bt.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(bt.TAG)

  override fun findAllLanguages(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .selectDistinct(sd.LANGUAGE)
      .from(sd)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
      .where(sd.LANGUAGE.ne(""))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.LANGUAGE)
      .fetchSet(sd.LANGUAGE)

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

  override fun findAllPublishers(filterOnLibraryIds: Collection<String>?): Set<String> =
    dslRO
      .selectDistinct(sd.PUBLISHER)
      .from(sd)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(sd.SERIES_ID.eq(s.ID)) } }
      .where(sd.PUBLISHER.ne(""))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(sd.PUBLISHER.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(sd.PUBLISHER)

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
    val sort = sd.PUBLISHER.collate(SqliteUdfDataSource.COLLATION_UNICODE_3)

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
      .orderBy(sd.PUBLISHER.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(sd.PUBLISHER)

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
      .orderBy(sd.PUBLISHER.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(sd.PUBLISHER)

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

  override fun findAllSeriesReleaseDates(filterOnLibraryIds: Collection<String>?): Set<LocalDate> =
    dslRO
      .selectDistinct(bma.RELEASE_DATE)
      .from(bma)
      .apply { filterOnLibraryIds?.let { leftJoin(s).on(bma.SERIES_ID.eq(s.ID)) } }
      .where(bma.RELEASE_DATE.isNotNull)
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .orderBy(bma.RELEASE_DATE.desc())
      .fetchSet(bma.RELEASE_DATE)

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
      }.orderBy(sl.LABEL.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(sl.LABEL)

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
      .orderBy(sl.LABEL.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(sl.LABEL)

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
      .orderBy(sl.LABEL.collate(SqliteUdfDataSource.COLLATION_UNICODE_3))
      .fetchSet(sl.LABEL)

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
