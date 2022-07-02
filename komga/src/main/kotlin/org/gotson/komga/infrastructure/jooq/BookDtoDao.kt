package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.search.LuceneEntity
import org.gotson.komga.infrastructure.search.LuceneHelper
import org.gotson.komga.infrastructure.web.toFilePath
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.AuthorDto
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.BookMetadataDto
import org.gotson.komga.interfaces.api.rest.dto.MediaDto
import org.gotson.komga.interfaces.api.rest.dto.ReadProgressDto
import org.gotson.komga.interfaces.api.rest.dto.WebLinkDto
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.BookMetadataRecord
import org.gotson.komga.jooq.tables.records.BookRecord
import org.gotson.komga.jooq.tables.records.MediaRecord
import org.gotson.komga.jooq.tables.records.ReadProgressRecord
import org.jooq.AggregateFunction
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.impl.DSL
import org.jooq.impl.DSL.inline
import org.jooq.impl.DSL.noCondition
import org.jooq.impl.DSL.param
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.net.URL

@Component
class BookDtoDao(
  private val dsl: DSLContext,
  private val luceneHelper: LuceneHelper,
) : BookDtoRepository {

  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val r = Tables.READ_PROGRESS
  private val a = Tables.BOOK_METADATA_AUTHOR
  private val s = Tables.SERIES
  private val sd = Tables.SERIES_METADATA
  private val rlb = Tables.READLIST_BOOK
  private val bt = Tables.BOOK_METADATA_TAG
  private val bl = Tables.BOOK_METADATA_LINK

  private val countUnread: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isNull, 1).otherwise(0))
  private val countRead: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isTrue, 1).otherwise(0))
  private val countInProgress: AggregateFunction<BigDecimal> = DSL.sum(DSL.`when`(r.COMPLETED.isFalse, 1).otherwise(0))

  private val sorts = mapOf(
    "name" to b.NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "created" to b.CREATED_DATE,
    "createdDate" to b.CREATED_DATE,
    "lastModified" to b.LAST_MODIFIED_DATE,
    "lastModifiedDate" to b.LAST_MODIFIED_DATE,
    "fileSize" to b.FILE_SIZE,
    "size" to b.FILE_SIZE,
    "fileHash" to b.FILE_HASH,
    "url" to b.URL.noCase(),
    "media.status" to m.STATUS.noCase(),
    "media.comment" to m.COMMENT.noCase(),
    "media.mediaType" to m.MEDIA_TYPE.noCase(),
    "metadata.title" to d.TITLE.collate(SqliteUdfDataSource.collationUnicode3),
    "metadata.numberSort" to d.NUMBER_SORT,
    "metadata.releaseDate" to d.RELEASE_DATE,
    "readProgress.lastModified" to r.LAST_MODIFIED_DATE,
    "readProgress.readDate" to r.READ_DATE,
    "readList.number" to rlb.NUMBER,
  )

  override fun findAll(search: BookSearchWithReadProgress, userId: String, pageable: Pageable, restrictions: ContentRestrictions): Page<BookDto> {
    val conditions = search.toCondition().and(restrictions.toCondition(dsl))

    return findAll(conditions, userId, pageable, search.toJoinConditions(), null, search.searchTerm)
  }

  override fun findAllByReadListId(
    readListId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    search: BookSearchWithReadProgress,
    pageable: Pageable,
  ): Page<BookDto> {
    val conditions = rlb.READLIST_ID.eq(readListId).and(search.toCondition())

    return findAll(conditions, userId, pageable, search.toJoinConditions().copy(selectReadListNumber = true), filterOnLibraryIds, search.searchTerm)
  }

  private fun findAll(
    conditions: Condition,
    userId: String,
    pageable: Pageable,
    joinConditions: JoinConditions = JoinConditions(),
    filterOnLibraryIds: Collection<String>?,
    searchTerm: String?,
  ): Page<BookDto> {
    val bookIds = luceneHelper.searchEntitiesIds(searchTerm, LuceneEntity.Book)
    val searchCondition = b.ID.inOrNoCondition(bookIds)

    val count = dsl.fetchCount(
      dsl.selectDistinct(b.ID)
        .from(b)
        .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
        .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
        .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
        .leftJoin(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
        .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
        .apply { if (joinConditions.tag) leftJoin(bt).on(b.ID.eq(bt.BOOK_ID)) }
        .apply { if (joinConditions.selectReadListNumber) leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID)) }
        .apply { if (joinConditions.author) leftJoin(a).on(b.ID.eq(a.BOOK_ID)) }
        .where(conditions)
        .and(searchCondition)
        .groupBy(b.ID),
    )

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !bookIds.isNullOrEmpty()) b.ID.sortByValues(bookIds, it.isAscending)
        else it.toSortField(sorts)
      }

    var dtos = listOf<BookDto>()
    dsl.connection {
      val ctx = DSL.using(it)
      dtos = selectBase(ctx, userId, joinConditions)
        .where(conditions)
        .and(searchCondition)
        .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchAndMap(ctx)
    }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      dtos,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findByIdOrNull(bookId: String, userId: String): BookDto? {
    var dto: BookDto? = null
    dsl.connection {
      val ctx = DSL.using(it)
      dto = selectBase(ctx, userId)
        .where(b.ID.eq(bookId))
        .fetchAndMap(ctx)
        .firstOrNull()
    }
    return dto
  }

  override fun findPreviousInSeriesOrNull(bookId: String, userId: String): BookDto? =
    findSiblingSeries(bookId, userId, next = false)

  override fun findNextInSeriesOrNull(bookId: String, userId: String): BookDto? =
    findSiblingSeries(bookId, userId, next = true)

  override fun findPreviousInReadListOrNull(
    readListId: String,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
  ): BookDto? =
    findSiblingReadList(readListId, bookId, userId, filterOnLibraryIds, next = false)

  override fun findNextInReadListOrNull(
    readListId: String,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
  ): BookDto? =
    findSiblingReadList(readListId, bookId, userId, filterOnLibraryIds, next = true)

  override fun findAllOnDeck(userId: String, filterOnLibraryIds: Collection<String>?, pageable: Pageable, restrictions: ContentRestrictions): Page<BookDto> {
    val seriesIds = dsl.select(s.ID)
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
      .leftJoin(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
      .where(restrictions.toCondition(dsl))
      .apply { filterOnLibraryIds?.let { and(s.LIBRARY_ID.`in`(it)) } }
      .groupBy(s.ID)
      .having(countUnread.ge(inline(1.toBigDecimal())))
      .and(countRead.ge(inline(1.toBigDecimal())))
      .and(countInProgress.eq(inline(0.toBigDecimal())))
      .orderBy(DSL.max(r.LAST_MODIFIED_DATE).desc())
      .fetchInto(String::class.java)

    var dtos = listOf<BookDto>()
    dsl.connection {
      val ctx = DSL.using(it)
      dtos = seriesIds
        .drop(pageable.pageNumber * pageable.pageSize)
        .take(pageable.pageSize)
        .mapNotNull { seriesId ->
          selectBase(ctx, userId)
            .where(b.SERIES_ID.eq(seriesId))
            .and(r.COMPLETED.isNull)
            .orderBy(d.NUMBER_SORT.asc())
            .limit(1)
            .fetchAndMap(ctx)
            .firstOrNull()
        }
    }

    return PageImpl(
      dtos,
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageable.sort),
      seriesIds.size.toLong(),
    )
  }

  override fun findAllDuplicates(userId: String, pageable: Pageable): Page<BookDto> {
    val hashes = dsl.select(b.FILE_HASH, DSL.count(b.ID))
      .from(b)
      .where(b.FILE_HASH.ne(""))
      .groupBy(b.FILE_HASH, b.FILE_SIZE)
      .having(DSL.count(b.ID).gt(1))
      .fetch()
      .associate { it.value1() to it.value2() }

    val count = hashes.values.sum()

    val orderBy = pageable.sort.toOrderBy(sorts)

    var dtos = listOf<BookDto>()
    dsl.connection {
      val ctx = DSL.using(it)
      dtos = selectBase(ctx, userId)
        .where(b.FILE_HASH.`in`(hashes.keys))
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchAndMap(ctx)
    }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      dtos,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)

  private fun findSiblingSeries(bookId: String, userId: String, next: Boolean): BookDto? {
    val record = dsl.select(b.SERIES_ID, d.NUMBER_SORT)
      .from(b)
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .where(b.ID.eq(bookId))
      .fetchOne()!!
    val seriesId = record.get(0, String::class.java)
    val numberSort = record.get(1, Float::class.java)

    var dto: BookDto? = null
    dsl.connection {
      val ctx = DSL.using(it)
      dto = selectBase(ctx, userId)
        .where(b.SERIES_ID.eq(seriesId))
        .orderBy(d.NUMBER_SORT.let { if (next) it.asc() else it.desc() })
        .seek(numberSort)
        .limit(1)
        .fetchAndMap(ctx)
        .firstOrNull()
    }
    return dto
  }

  private fun findSiblingReadList(
    readListId: String,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    next: Boolean,
  ): BookDto? {
    val numberSort = dsl.select(rlb.NUMBER)
      .from(b)
      .leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID))
      .where(b.ID.eq(bookId))
      .and(rlb.READLIST_ID.eq(readListId))
      .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
      .fetchOne(0, Int::class.java)

    var dto: BookDto? = null
    dsl.connection {
      val ctx = DSL.using(it)
      dto = selectBase(ctx, userId, JoinConditions(selectReadListNumber = true))
        .where(rlb.READLIST_ID.eq(readListId))
        .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
        .orderBy(rlb.NUMBER.let { if (next) it.asc() else it.desc() })
        .seek(numberSort)
        .limit(1)
        .fetchAndMap(ctx)
        .firstOrNull()
    }
    return dto
  }

  private fun selectBase(ctx: DSLContext, userId: String, joinConditions: JoinConditions = JoinConditions()) =
    ctx.selectDistinct(
      *b.fields(),
      *m.fields(),
      *d.fields(),
      *r.fields(),
      sd.TITLE,
    ).apply { if (joinConditions.selectReadListNumber) select(rlb.NUMBER) }
      .from(b)
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
      .leftJoin(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
      .apply { if (joinConditions.tag) leftJoin(bt).on(b.ID.eq(bt.BOOK_ID)) }
      .apply { if (joinConditions.selectReadListNumber) leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID)) }
      .apply { if (joinConditions.author) leftJoin(a).on(b.ID.eq(a.BOOK_ID)) }

  private fun ResultQuery<Record>.fetchAndMap(ctx: DSLContext) =
    fetch().let { ret ->
      val qAuthors = ctx.selectFrom(a).where(a.BOOK_ID.eq(param("id", ""))).keepStatement(true)
      val qTags = ctx.select(bt.TAG).from(bt).where(bt.BOOK_ID.eq(param("id", ""))).keepStatement(true)
      val qLinks = ctx.select(bl.LABEL, bl.URL).from(bl).where(bl.BOOK_ID.eq(param("id", ""))).keepStatement(true)

      qLinks.use {
        qTags.use {
          qAuthors.use {
            ret.map { rec ->
              val br = rec.into(b)
              val mr = rec.into(m)
              val dr = rec.into(d)
              val rr = rec.into(r)
              val seriesTitle = rec.into(sd.TITLE).component1()

              val authors = qAuthors.bind("id", br.id)
                .fetchInto(a)
                .filter { it.name != null }
                .map { AuthorDto(it.name, it.role) }

              val tags = qTags.bind("id", br.id).fetchSet(bt.TAG)

              val links = qLinks.bind("id", br.id).fetchInto(bl).map { WebLinkDto(it.label, it.url) }

              br.toDto(mr.toDto(), dr.toDto(authors, tags, links), if (rr.userId != null) rr.toDto() else null, seriesTitle)
            }
          }
        }
      }
    }

  private fun BookSearchWithReadProgress.toCondition(): Condition {
    var c: Condition = noCondition()

    if (!libraryIds.isNullOrEmpty()) c = c.and(b.LIBRARY_ID.`in`(libraryIds))
    if (!seriesIds.isNullOrEmpty()) c = c.and(b.SERIES_ID.`in`(seriesIds))
    if (!mediaStatus.isNullOrEmpty()) c = c.and(m.STATUS.`in`(mediaStatus))
    if (deleted == true) c = c.and(b.DELETED_DATE.isNotNull)
    if (deleted == false) c = c.and(b.DELETED_DATE.isNull)
    if (releasedAfter != null) c = c.and(d.RELEASE_DATE.gt(releasedAfter))
    if (!tags.isNullOrEmpty()) c = c.and(bt.TAG.collate(SqliteUdfDataSource.collationUnicode3).`in`(tags))

    if (readStatus != null) {
      val cr = readStatus.map {
        when (it) {
          ReadStatus.UNREAD -> r.COMPLETED.isNull
          ReadStatus.READ -> r.COMPLETED.isTrue
          ReadStatus.IN_PROGRESS -> r.COMPLETED.isFalse
        }
      }.reduce { acc, condition -> acc.or(condition) }

      c = c.and(cr)
    }

    if (!authors.isNullOrEmpty()) {
      var ca = noCondition()
      authors.forEach {
        ca = ca.or(a.NAME.equalIgnoreCase(it.name).and(a.ROLE.equalIgnoreCase(it.role)))
      }
      c = c.and(ca)
    }

    return c
  }

  private fun BookSearchWithReadProgress.toJoinConditions() =
    JoinConditions(
      tag = !tags.isNullOrEmpty(),
      author = !authors.isNullOrEmpty(),
    )

  private data class JoinConditions(
    val selectReadListNumber: Boolean = false,
    val tag: Boolean = false,
    val author: Boolean = false,
  )

  private fun BookRecord.toDto(media: MediaDto, metadata: BookMetadataDto, readProgress: ReadProgressDto?, seriesTitle: String) =
    BookDto(
      id = id,
      seriesId = seriesId,
      seriesTitle = seriesTitle,
      libraryId = libraryId,
      name = name,
      url = URL(url).toFilePath(),
      number = number,
      created = createdDate,
      lastModified = lastModifiedDate,
      fileLastModified = fileLastModified,
      sizeBytes = fileSize,
      media = media,
      metadata = metadata,
      readProgress = readProgress,
      deleted = deletedDate != null,
      fileHash = fileHash,
    )

  private fun MediaRecord.toDto() =
    MediaDto(
      status = status,
      mediaType = mediaType ?: "",
      pagesCount = pageCount.toInt(),
      comment = comment ?: "",
    )

  private fun BookMetadataRecord.toDto(authors: List<AuthorDto>, tags: Set<String>, links: List<WebLinkDto>) =
    BookMetadataDto(
      title = title,
      titleLock = titleLock,
      summary = summary,
      summaryLock = summaryLock,
      number = number,
      numberLock = numberLock,
      numberSort = numberSort,
      numberSortLock = numberSortLock,
      releaseDate = releaseDate,
      releaseDateLock = releaseDateLock,
      authors = authors,
      authorsLock = authorsLock,
      tags = tags,
      tagsLock = tagsLock,
      isbn = isbn,
      isbnLock = isbnLock,
      links = links,
      linksLock = linksLock,
      created = createdDate,
      lastModified = lastModifiedDate,
    )

  private fun ReadProgressRecord.toDto() =
    ReadProgressDto(
      page = page,
      completed = completed,
      readDate = readDate,
      created = createdDate,
      lastModified = lastModifiedDate,
    )
}
