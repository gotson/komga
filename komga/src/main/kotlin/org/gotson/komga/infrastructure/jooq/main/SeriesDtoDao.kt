package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchField
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.RequiredJoin
import org.gotson.komga.infrastructure.jooq.SeriesSearchHelper
import org.gotson.komga.infrastructure.jooq.SplitDslDaoBase
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.infrastructure.jooq.csAlias
import org.gotson.komga.infrastructure.jooq.inOrNoCondition
import org.gotson.komga.infrastructure.jooq.sortByValues
import org.gotson.komga.infrastructure.jooq.toSortField
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.gotson.komga.infrastructure.search.LuceneHelper
import org.gotson.komga.infrastructure.web.toFilePath
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.AlternateTitleDto
import org.gotson.komga.interfaces.api.rest.dto.AuthorDto
import org.gotson.komga.interfaces.api.rest.dto.BookMetadataAggregationDto
import org.gotson.komga.interfaces.api.rest.dto.GroupCountDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesMetadataDto
import org.gotson.komga.interfaces.api.rest.dto.WebLinkDto
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookMetadataAggregationRecord
import org.gotson.komga.jooq.main.tables.records.SeriesMetadataRecord
import org.gotson.komga.jooq.main.tables.records.SeriesRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.SelectOnConditionStep
import org.jooq.impl.DSL
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.countDistinct
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.substring
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.net.URL

const val BOOKS_UNREAD_COUNT = "booksUnreadCount"
const val BOOKS_IN_PROGRESS_COUNT = "booksInProgressCount"
const val BOOKS_READ_COUNT = "booksReadCount"

@Component
class SeriesDtoDao(
  dslRW: DSLContext,
  @Qualifier("dslContextRO") dslRO: DSLContext,
  private val luceneHelper: LuceneHelper,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SplitDslDaoBase(dslRW, dslRO),
  SeriesDtoRepository {
  private val s = Tables.SERIES
  private val d = Tables.SERIES_METADATA
  private val rs = Tables.READ_PROGRESS_SERIES
  private val cs = Tables.COLLECTION_SERIES
  private val g = Tables.SERIES_METADATA_GENRE
  private val st = Tables.SERIES_METADATA_TAG
  private val sl = Tables.SERIES_METADATA_SHARING
  private val slk = Tables.SERIES_METADATA_LINK
  private val sat = Tables.SERIES_METADATA_ALTERNATE_TITLE
  private val bma = Tables.BOOK_METADATA_AGGREGATION
  private val bmaa = Tables.BOOK_METADATA_AGGREGATION_AUTHOR
  private val bmat = Tables.BOOK_METADATA_AGGREGATION_TAG

  private val groupFields =
    arrayOf(
      *s.fields(),
      *d.fields(),
      *bma.fields(),
      *rs.fields(),
    )

  private val sorts =
    mapOf(
      "metadata.titleSort" to d.TITLE_SORT.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
      "createdDate" to s.CREATED_DATE,
      "created" to s.CREATED_DATE,
      "lastModifiedDate" to s.LAST_MODIFIED_DATE,
      "lastModified" to s.LAST_MODIFIED_DATE,
      "booksMetadata.releaseDate" to bma.RELEASE_DATE,
      "readDate" to rs.MOST_RECENT_READ_DATE,
      "collection.number" to cs.NUMBER,
      "name" to s.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
      "booksCount" to s.BOOK_COUNT,
      "random" to DSL.rand(),
    )

  override fun findAll(pageable: Pageable): Page<SeriesDto> = findAll(SeriesSearch(), SearchContext.ofAnonymousUser(), pageable)

  override fun findAll(
    context: SearchContext,
    pageable: Pageable,
  ): Page<SeriesDto> = findAll(SeriesSearch(), context, pageable)

  override fun findAll(
    search: SeriesSearch,
    context: SearchContext,
    pageable: Pageable,
  ): Page<SeriesDto> {
    requireNotNull(context.userId) { "Missing userId in search context" }

    val (conditions, joins) = SeriesSearchHelper(context).toCondition(search.condition)
    val conditionsRefined = conditions.and(search.regexSearch?.let { it.second.toColumn().likeRegex(it.first) } ?: DSL.noCondition())

    return findAll(conditionsRefined, context.userId, pageable, joins, search.fullTextSearch)
  }

  override fun findAllRecentlyUpdated(
    search: SeriesSearch,
    context: SearchContext,
    pageable: Pageable,
  ): Page<SeriesDto> {
    requireNotNull(context.userId) { "Missing userId in search context" }

    val (conditions, joins) = SeriesSearchHelper(context).toCondition(search.condition)
    val conditionsRefined = conditions.and(s.CREATED_DATE.notEqual(s.LAST_MODIFIED_DATE))

    return findAll(conditionsRefined, context.userId, pageable, joins, search.fullTextSearch)
  }

  override fun countByFirstCharacter(
    search: SeriesSearch,
    context: SearchContext,
  ): List<GroupCountDto> {
    requireNotNull(context.userId) { "Missing userId in search context" }

    val (conditions, joins) = SeriesSearchHelper(context).toCondition(search.condition)
    val conditionsRefined = conditions.and(search.regexSearch?.let { it.second.toColumn().likeRegex(it.first) } ?: DSL.noCondition())

    val seriesIds = luceneHelper.searchEntitiesIds(search.fullTextSearch, LuceneEntity.Series)
    val searchCondition = s.ID.inOrNoCondition(seriesIds)

    val firstChar = lower(substring(d.TITLE_SORT, 1, 1))
    return dslRO
      .select(firstChar, count())
      .from(s)
      .leftJoin(d)
      .on(s.ID.eq(d.SERIES_ID))
      .leftJoin(bma)
      .on(s.ID.eq(bma.SERIES_ID))
      .leftJoin(rs)
      .on(s.ID.eq(rs.SERIES_ID))
      .and(readProgressConditionSeries(context.userId))
      .apply {
        joins.forEach { join ->
          when (join) {
            is RequiredJoin.Collection -> {
              val csAlias = csAlias(join.collectionId)
              leftJoin(csAlias).on(s.ID.eq(csAlias.SERIES_ID).and(csAlias.COLLECTION_ID.eq(join.collectionId)))
            }
            // always joined
            is RequiredJoin.ReadProgress -> Unit
            RequiredJoin.SeriesMetadata -> Unit
            // Book joins - not needed
            RequiredJoin.Media -> Unit
            RequiredJoin.BookMetadata -> Unit
            RequiredJoin.BookMetadataAggregation -> Unit
            is RequiredJoin.ReadList -> Unit
          }
        }
      }.where(conditionsRefined)
      .and(searchCondition)
      .groupBy(firstChar)
      .map {
        GroupCountDto(it.value1(), it.value2())
      }
  }

  override fun findByIdOrNull(
    seriesId: String,
    userId: String,
  ): SeriesDto? =
    dslRO
      .selectBase(userId)
      .where(s.ID.eq(seriesId))
      .groupBy(*groupFields)
      .fetchAndMap(dslRO)
      .firstOrNull()

  private fun DSLContext.selectBase(
    userId: String,
    joins: Set<RequiredJoin> = emptySet(),
  ): SelectOnConditionStep<Record> =
    this
      .select(*groupFields)
      .from(s)
      .leftJoin(d)
      .on(s.ID.eq(d.SERIES_ID))
      .leftJoin(bma)
      .on(s.ID.eq(bma.SERIES_ID))
      .leftJoin(rs)
      .on(s.ID.eq(rs.SERIES_ID))
      .and(readProgressConditionSeries(userId))
      .apply {
        joins.forEach { join ->
          when (join) {
            is RequiredJoin.Collection -> {
              val csAlias = csAlias(join.collectionId)
              leftJoin(csAlias).on(s.ID.eq(csAlias.SERIES_ID).and(csAlias.COLLECTION_ID.eq(join.collectionId)))
            }
            // always joined
            is RequiredJoin.ReadProgress -> Unit
            RequiredJoin.SeriesMetadata -> Unit
            // Book joins - not needed
            RequiredJoin.BookMetadata -> Unit
            RequiredJoin.BookMetadataAggregation -> Unit
            RequiredJoin.Media -> Unit
            is RequiredJoin.ReadList -> Unit
          }
        }
      }

  private fun findAll(
    conditions: Condition,
    userId: String,
    pageable: Pageable,
    joins: Set<RequiredJoin> = emptySet(),
    searchTerm: String? = null,
  ): Page<SeriesDto> {
    val seriesIds = luceneHelper.searchEntitiesIds(searchTerm, LuceneEntity.Series)
    val searchCondition = s.ID.inOrNoCondition(seriesIds)

    val count =
      dslRO
        .select(countDistinct(s.ID))
        .from(s)
        .leftJoin(d)
        .on(s.ID.eq(d.SERIES_ID))
        .leftJoin(bma)
        .on(s.ID.eq(bma.SERIES_ID))
        .leftJoin(rs)
        .on(s.ID.eq(rs.SERIES_ID))
        .and(readProgressConditionSeries(userId))
        .apply {
          joins.forEach { join ->
            when (join) {
              is RequiredJoin.Collection -> {
                val csAlias = csAlias(join.collectionId)
                leftJoin(csAlias).on(s.ID.eq(csAlias.SERIES_ID).and(csAlias.COLLECTION_ID.eq(join.collectionId)))
              }
              // always joined
              is RequiredJoin.ReadProgress -> Unit
              RequiredJoin.SeriesMetadata -> Unit
              // Book joins - not needed
              RequiredJoin.BookMetadata -> Unit
              RequiredJoin.BookMetadataAggregation -> Unit
              RequiredJoin.Media -> Unit
              is RequiredJoin.ReadList -> Unit
            }
          }
        }.where(conditions)
        .and(searchCondition)
        .fetchOne(countDistinct(s.ID)) ?: 0

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !seriesIds.isNullOrEmpty()) {
          s.ID.sortByValues(seriesIds, it.isAscending)
        } else {
          if (it.property == "collection.number") {
            val collectionId = joins.filterIsInstance<RequiredJoin.Collection>().firstOrNull()?.collectionId ?: return@mapNotNull null
            val f = csAlias(collectionId).NUMBER
            if (it.isAscending) f.asc() else f.desc()
          } else {
            it.toSortField(sorts)
          }
        }
      }

    val dtos =
      dslRO
        .selectBase(userId, joins)
        .where(conditions)
        .and(searchCondition)
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchAndMap(dslRO)

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      dtos,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  private fun readProgressConditionSeries(userId: String): Condition = rs.USER_ID.eq(userId).or(rs.USER_ID.isNull)

  private fun ResultQuery<Record>.fetchAndMap(dsl: DSLContext): MutableList<SeriesDto> {
    val records = fetch()
    val seriesIds = records.getValues(s.ID)

    lateinit var genres: Map<String, List<String>>
    lateinit var tags: Map<String, List<String>>
    lateinit var sharingLabels: Map<String, List<String>>
    lateinit var links: Map<String, List<WebLinkDto>>
    lateinit var alternateTitles: Map<String, List<AlternateTitleDto>>
    lateinit var aggregatedAuthors: Map<String, List<AuthorDto>>
    lateinit var aggregatedTags: Map<String, List<String>>

    dsl.withTempTable(batchSize, seriesIds).use { tempTable ->
      genres =
        dsl
          .selectFrom(g)
          .where(g.SERIES_ID.`in`(tempTable.selectTempStrings()))
          .groupBy({ it.seriesId }, { it.genre })

      tags =
        dsl
          .selectFrom(st)
          .where(st.SERIES_ID.`in`(tempTable.selectTempStrings()))
          .groupBy({ it.seriesId }, { it.tag })

      sharingLabels =
        dsl
          .selectFrom(sl)
          .where(sl.SERIES_ID.`in`(tempTable.selectTempStrings()))
          .groupBy({ it.seriesId }, { it.label })

      links =
        dsl
          .selectFrom(slk)
          .where(slk.SERIES_ID.`in`(tempTable.selectTempStrings()))
          .groupBy({ it.seriesId }, { WebLinkDto(it.label, it.url) })

      alternateTitles =
        dsl
          .selectFrom(sat)
          .where(sat.SERIES_ID.`in`(tempTable.selectTempStrings()))
          .groupBy({ it.seriesId }, { AlternateTitleDto(it.label, it.title) })

      aggregatedAuthors =
        dsl
          .selectFrom(bmaa)
          .where(bmaa.SERIES_ID.`in`(tempTable.selectTempStrings()))
          .filter { it.name != null }
          .groupBy({ it.seriesId }, { AuthorDto(it.name, it.role) })

      aggregatedTags =
        dsl
          .selectFrom(bmat)
          .where(bmat.SERIES_ID.`in`(tempTable.selectTempStrings()))
          .groupBy({ it.seriesId }, { it.tag })
    }

    return records
      .map { rec ->
        val sr = rec.into(s)
        val dr = rec.into(d)
        val bmar = rec.into(bma)
        val rsr = rec.into(rs)
        val booksReadCount = rsr.readCount ?: 0
        val booksInProgressCount = rsr.inProgressCount ?: 0
        val booksUnreadCount = sr.bookCount - booksReadCount - booksInProgressCount

        sr.toDto(
          sr.bookCount,
          booksReadCount,
          booksUnreadCount,
          booksInProgressCount,
          dr.toDto(genres[sr.id].orEmpty().toSet(), tags[sr.id].orEmpty().toSet(), sharingLabels[sr.id].orEmpty().toSet(), links[sr.id].orEmpty(), alternateTitles[sr.id].orEmpty()),
          bmar.toDto(aggregatedAuthors[sr.id].orEmpty(), aggregatedTags[sr.id].orEmpty().toSet()),
        )
      }
  }

  private fun SearchField.toColumn() =
    when (this) {
      SearchField.TITLE -> d.TITLE
      SearchField.TITLE_SORT -> d.TITLE_SORT
    }

  private fun SeriesRecord.toDto(
    booksCount: Int,
    booksReadCount: Int,
    booksUnreadCount: Int,
    booksInProgressCount: Int,
    metadata: SeriesMetadataDto,
    booksMetadata: BookMetadataAggregationDto,
  ) = SeriesDto(
    id = id,
    libraryId = libraryId,
    name = name,
    url = URL(url).toFilePath(),
    created = createdDate,
    lastModified = lastModifiedDate,
    fileLastModified = fileLastModified,
    booksCount = booksCount,
    booksReadCount = booksReadCount,
    booksUnreadCount = booksUnreadCount,
    booksInProgressCount = booksInProgressCount,
    metadata = metadata,
    booksMetadata = booksMetadata,
    deleted = deletedDate != null,
    oneshot = oneshot,
  )

  private fun SeriesMetadataRecord.toDto(
    genres: Set<String>,
    tags: Set<String>,
    sharingLabels: Set<String>,
    links: List<WebLinkDto>,
    alternateTitles: List<AlternateTitleDto>,
  ) = SeriesMetadataDto(
    status = status,
    statusLock = statusLock,
    created = createdDate,
    lastModified = lastModifiedDate,
    title = title,
    titleLock = titleLock,
    titleSort = titleSort,
    titleSortLock = titleSortLock,
    summary = summary,
    summaryLock = summaryLock,
    readingDirection = readingDirection ?: "",
    readingDirectionLock = readingDirectionLock,
    publisher = publisher,
    publisherLock = publisherLock,
    ageRating = ageRating,
    ageRatingLock = ageRatingLock,
    language = language,
    languageLock = languageLock,
    genres = genres,
    genresLock = genresLock,
    tags = tags,
    tagsLock = tagsLock,
    totalBookCount = totalBookCount,
    totalBookCountLock = totalBookCountLock,
    sharingLabels = sharingLabels,
    sharingLabelsLock = sharingLabelsLock,
    links = links,
    linksLock = linksLock,
    alternateTitles = alternateTitles,
    alternateTitlesLock = alternateTitlesLock,
  )

  private fun BookMetadataAggregationRecord.toDto(
    authors: List<AuthorDto>,
    tags: Set<String>,
  ) = BookMetadataAggregationDto(
    authors = authors,
    tags = tags,
    releaseDate = releaseDate,
    summary = summary,
    summaryNumber = summaryNumber,
    created = createdDate,
    lastModified = lastModifiedDate,
  )
}
