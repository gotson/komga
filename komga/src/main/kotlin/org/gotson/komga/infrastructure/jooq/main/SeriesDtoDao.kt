package org.gotson.komga.infrastructure.jooq.main

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.inOrNoCondition
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.noCase
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.infrastructure.jooq.sortByValues
import org.gotson.komga.infrastructure.jooq.toCondition
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.net.URL

private val logger = KotlinLogging.logger {}

const val BOOKS_UNREAD_COUNT = "booksUnreadCount"
const val BOOKS_IN_PROGRESS_COUNT = "booksInProgressCount"
const val BOOKS_READ_COUNT = "booksReadCount"

@Component
class SeriesDtoDao(
  private val dsl: DSLContext,
  private val luceneHelper: LuceneHelper,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
  private val transactionTemplate: TransactionTemplate,
) : SeriesDtoRepository {
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
      "metadata.titleSort" to d.TITLE_SORT.noCase(),
      "createdDate" to s.CREATED_DATE,
      "created" to s.CREATED_DATE,
      "lastModifiedDate" to s.LAST_MODIFIED_DATE,
      "lastModified" to s.LAST_MODIFIED_DATE,
      "booksMetadata.releaseDate" to bma.RELEASE_DATE,
      "collection.number" to cs.NUMBER,
      "name" to s.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
      "booksCount" to s.BOOK_COUNT,
    )

  override fun findAll(
    search: SeriesSearchWithReadProgress,
    userId: String,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<SeriesDto> {
    val conditions = search.toCondition().and(restrictions.toCondition(dsl))

    return findAll(conditions, userId, pageable, search.toJoinConditions(), search.searchTerm)
  }

  override fun findAllByCollectionId(
    collectionId: String,
    search: SeriesSearchWithReadProgress,
    userId: String,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<SeriesDto> {
    val conditions = search.toCondition().and(restrictions.toCondition(dsl)).and(cs.COLLECTION_ID.eq(collectionId))
    val joinConditions = search.toJoinConditions().copy(selectCollectionNumber = true, collection = true)

    return findAll(conditions, userId, pageable, joinConditions, search.searchTerm)
  }

  override fun findAllRecentlyUpdated(
    search: SeriesSearchWithReadProgress,
    userId: String,
    restrictions: ContentRestrictions,
    pageable: Pageable,
  ): Page<SeriesDto> {
    val conditions =
      search.toCondition()
        .and(restrictions.toCondition(dsl))
        .and(s.CREATED_DATE.notEqual(s.LAST_MODIFIED_DATE))

    return findAll(conditions, userId, pageable, search.toJoinConditions(), search.searchTerm)
  }

  override fun countByFirstCharacter(
    search: SeriesSearchWithReadProgress,
    userId: String,
    restrictions: ContentRestrictions,
  ): List<GroupCountDto> {
    val conditions = search.toCondition().and(restrictions.toCondition(dsl))
    val joinConditions = search.toJoinConditions()
    val seriesIds = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.Series)
    val searchCondition = s.ID.inOrNoCondition(seriesIds)

    val firstChar = lower(substring(d.TITLE_SORT, 1, 1))
    return dsl.select(firstChar, count())
      .from(s)
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .leftJoin(bma).on(s.ID.eq(bma.SERIES_ID))
      .leftJoin(rs).on(s.ID.eq(rs.SERIES_ID)).and(readProgressConditionSeries(userId))
      .apply { if (joinConditions.genre) leftJoin(g).on(s.ID.eq(g.SERIES_ID)) }
      .apply {
        if (joinConditions.tag)
          leftJoin(st).on(s.ID.eq(st.SERIES_ID))
            .leftJoin(bmat).on(s.ID.eq(bmat.SERIES_ID))
      }
      .apply { if (joinConditions.collection) leftJoin(cs).on(s.ID.eq(cs.SERIES_ID)) }
      .apply { if (joinConditions.aggregationAuthor) leftJoin(bmaa).on(s.ID.eq(bmaa.SERIES_ID)) }
      .apply { if (joinConditions.sharingLabel) leftJoin(sl).on(s.ID.eq(sl.SERIES_ID)) }
      .where(conditions)
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
    selectBase(userId)
      .where(s.ID.eq(seriesId))
      .groupBy(*groupFields)
      .fetchAndMap()
      .firstOrNull()

  private fun selectBase(
    userId: String,
    joinConditions: JoinConditions = JoinConditions(),
  ): SelectOnConditionStep<Record> =
    dsl.selectDistinct(*groupFields)
      .apply { if (joinConditions.selectCollectionNumber) select(cs.NUMBER) }
      .from(s)
      .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
      .leftJoin(bma).on(s.ID.eq(bma.SERIES_ID))
      .leftJoin(rs).on(s.ID.eq(rs.SERIES_ID)).and(readProgressConditionSeries(userId))
      .apply { if (joinConditions.genre) leftJoin(g).on(s.ID.eq(g.SERIES_ID)) }
      .apply {
        if (joinConditions.tag)
          leftJoin(st).on(s.ID.eq(st.SERIES_ID))
            .leftJoin(bmat).on(s.ID.eq(bmat.SERIES_ID))
      }
      .apply { if (joinConditions.collection) leftJoin(cs).on(s.ID.eq(cs.SERIES_ID)) }
      .apply { if (joinConditions.aggregationAuthor) leftJoin(bmaa).on(s.ID.eq(bmaa.SERIES_ID)) }
      .apply { if (joinConditions.sharingLabel) leftJoin(sl).on(s.ID.eq(sl.SERIES_ID)) }

  private fun findAll(
    conditions: Condition,
    userId: String,
    pageable: Pageable,
    joinConditions: JoinConditions = JoinConditions(),
    searchTerm: String?,
  ): Page<SeriesDto> {
    val seriesIds = luceneHelper.searchEntitiesIds(searchTerm, LuceneEntity.Series)
    val searchCondition = s.ID.inOrNoCondition(seriesIds)

    val count =
      dsl.select(countDistinct(s.ID))
        .from(s)
        .leftJoin(d).on(s.ID.eq(d.SERIES_ID))
        .leftJoin(bma).on(s.ID.eq(bma.SERIES_ID))
        .leftJoin(rs).on(s.ID.eq(rs.SERIES_ID)).and(readProgressConditionSeries(userId))
        .apply { if (joinConditions.genre) leftJoin(g).on(s.ID.eq(g.SERIES_ID)) }
        .apply {
          if (joinConditions.tag)
            leftJoin(st).on(s.ID.eq(st.SERIES_ID))
              .leftJoin(bmat).on(s.ID.eq(bmat.SERIES_ID))
        }
        .apply { if (joinConditions.collection) leftJoin(cs).on(s.ID.eq(cs.SERIES_ID)) }
        .apply { if (joinConditions.aggregationAuthor) leftJoin(bmaa).on(s.ID.eq(bmaa.SERIES_ID)) }
        .apply { if (joinConditions.sharingLabel) leftJoin(sl).on(s.ID.eq(sl.SERIES_ID)) }
        .where(conditions)
        .and(searchCondition)
        .fetchOne(countDistinct(s.ID)) ?: 0

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !seriesIds.isNullOrEmpty())
          s.ID.sortByValues(seriesIds, it.isAscending)
        else
          it.toSortField(sorts)
      }

    val dtos =
      selectBase(userId, joinConditions)
        .where(conditions)
        .and(searchCondition)
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchAndMap()

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

  private fun ResultQuery<Record>.fetchAndMap(): MutableList<SeriesDto> {
    val records = fetch()
    val seriesIds = records.getValues(s.ID)

    lateinit var genres: Map<String, List<String>>
    lateinit var tags: Map<String, List<String>>
    lateinit var sharingLabels: Map<String, List<String>>
    lateinit var links: Map<String, List<WebLinkDto>>
    lateinit var alternateTitles: Map<String, List<AlternateTitleDto>>
    lateinit var aggregatedAuthors: Map<String, List<AuthorDto>>
    lateinit var aggregatedTags: Map<String, List<String>>
    transactionTemplate.executeWithoutResult {
      dsl.insertTempStrings(batchSize, seriesIds)
      genres =
        dsl.selectFrom(g)
          .where(g.SERIES_ID.`in`(dsl.selectTempStrings()))
          .groupBy({ it.seriesId }, { it.genre })

      tags =
        dsl.selectFrom(st)
          .where(st.SERIES_ID.`in`(dsl.selectTempStrings()))
          .groupBy({ it.seriesId }, { it.tag })

      sharingLabels =
        dsl.selectFrom(sl)
          .where(sl.SERIES_ID.`in`(dsl.selectTempStrings()))
          .groupBy({ it.seriesId }, { it.label })

      links =
        dsl.selectFrom(slk)
          .where(slk.SERIES_ID.`in`(dsl.selectTempStrings()))
          .groupBy({ it.seriesId }, { WebLinkDto(it.label, it.url) })

      alternateTitles =
        dsl.selectFrom(sat)
          .where(sat.SERIES_ID.`in`(dsl.selectTempStrings()))
          .groupBy({ it.seriesId }, { AlternateTitleDto(it.label, it.title) })

      aggregatedAuthors =
        dsl.selectFrom(bmaa)
          .where(bmaa.SERIES_ID.`in`(dsl.selectTempStrings()))
          .filter { it.name != null }
          .groupBy({ it.seriesId }, { AuthorDto(it.name, it.role) })

      aggregatedTags =
        dsl.selectFrom(bmat)
          .where(bmat.SERIES_ID.`in`(dsl.selectTempStrings()))
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

  private fun SeriesSearchWithReadProgress.toCondition(): Condition {
    var c = DSL.noCondition()

    if (libraryIds != null) c = c.and(s.LIBRARY_ID.`in`(libraryIds))
    if (!collectionIds.isNullOrEmpty()) c = c.and(cs.COLLECTION_ID.`in`(collectionIds))
    searchRegex?.let { c = c.and((it.second.toColumn()).likeRegex(it.first)) }
    if (!metadataStatus.isNullOrEmpty()) c = c.and(d.STATUS.`in`(metadataStatus))
    if (!publishers.isNullOrEmpty()) c = c.and(d.PUBLISHER.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(publishers))
    if (deleted == true) c = c.and(s.DELETED_DATE.isNotNull)
    if (deleted == false) c = c.and(s.DELETED_DATE.isNull)
    if (complete == false) c = c.and(d.TOTAL_BOOK_COUNT.isNotNull.and(d.TOTAL_BOOK_COUNT.ne(s.BOOK_COUNT)))
    if (complete == true) c = c.and(d.TOTAL_BOOK_COUNT.isNotNull.and(d.TOTAL_BOOK_COUNT.eq(s.BOOK_COUNT)))
    if (oneshot != null) c = c.and(s.ONESHOT.eq(oneshot))
    if (!languages.isNullOrEmpty()) c = c.and(d.LANGUAGE.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(languages))
    if (!genres.isNullOrEmpty()) c = c.and(g.GENRE.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(genres))
    if (!tags.isNullOrEmpty()) c = c.and(st.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(tags).or(bmat.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(tags)))
    if (!ageRatings.isNullOrEmpty()) {
      val c1 = if (ageRatings.contains(null)) d.AGE_RATING.isNull else DSL.noCondition()
      val c2 = if (ageRatings.filterNotNull().isNotEmpty()) d.AGE_RATING.`in`(ageRatings.filterNotNull()) else DSL.noCondition()
      c = c.and(c1.or(c2))
    }
    // cast to String is necessary for SQLite, else the years in the IN block are coerced to Int, even though YEAR for SQLite uses strftime (string)
    if (!releaseYears.isNullOrEmpty()) c = c.and(DSL.year(bma.RELEASE_DATE).cast(String::class.java).`in`(releaseYears))
    if (!authors.isNullOrEmpty()) {
      var ca = DSL.noCondition()
      authors.forEach {
        ca = ca.or(bmaa.NAME.equalIgnoreCase(it.name).and(bmaa.ROLE.equalIgnoreCase(it.role)))
      }
      c = c.and(ca)
    }
    if (!sharingLabels.isNullOrEmpty()) c = c.and(sl.LABEL.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(sharingLabels))
    if (!readStatus.isNullOrEmpty()) {
      val cr =
        readStatus.map {
          when (it) {
            ReadStatus.UNREAD -> rs.READ_COUNT.isNull
            ReadStatus.READ -> rs.READ_COUNT.eq(s.BOOK_COUNT)
            ReadStatus.IN_PROGRESS -> rs.READ_COUNT.ne(s.BOOK_COUNT)
          }
        }.reduce { acc, condition -> acc.or(condition) }
      c = c.and(cr)
    }

    return c
  }

  private fun SeriesSearch.SearchField.toColumn() =
    when (this) {
      SeriesSearch.SearchField.NAME -> s.NAME
      SeriesSearch.SearchField.TITLE -> d.TITLE
      SeriesSearch.SearchField.TITLE_SORT -> d.TITLE_SORT
    }

  private fun SeriesSearchWithReadProgress.toJoinConditions() =
    JoinConditions(
      genre = !genres.isNullOrEmpty(),
      tag = !tags.isNullOrEmpty(),
      collection = !collectionIds.isNullOrEmpty(),
      aggregationAuthor = !authors.isNullOrEmpty(),
      sharingLabel = !sharingLabels.isNullOrEmpty(),
    )

  private data class JoinConditions(
    val selectCollectionNumber: Boolean = false,
    val genre: Boolean = false,
    val tag: Boolean = false,
    val collection: Boolean = false,
    val aggregationAuthor: Boolean = false,
    val sharingLabel: Boolean = false,
  )

  private fun SeriesRecord.toDto(
    booksCount: Int,
    booksReadCount: Int,
    booksUnreadCount: Int,
    booksInProgressCount: Int,
    metadata: SeriesMetadataDto,
    booksMetadata: BookMetadataAggregationDto,
  ) =
    SeriesDto(
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
  ) =
    SeriesMetadataDto(
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
  ) =
    BookMetadataAggregationDto(
      authors = authors,
      tags = tags,
      releaseDate = releaseDate,
      summary = summary,
      summaryNumber = summaryNumber,
      created = createdDate,
      lastModified = lastModifiedDate,
    )
}
