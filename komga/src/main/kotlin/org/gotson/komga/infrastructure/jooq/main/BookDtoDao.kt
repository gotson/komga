package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.noCase
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.infrastructure.jooq.sortByValues
import org.gotson.komga.infrastructure.jooq.toCondition
import org.gotson.komga.infrastructure.jooq.toOrderBy
import org.gotson.komga.infrastructure.jooq.toSortField
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
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookMetadataRecord
import org.gotson.komga.jooq.main.tables.records.BookRecord
import org.gotson.komga.jooq.main.tables.records.MediaRecord
import org.gotson.komga.jooq.main.tables.records.ReadProgressRecord
import org.gotson.komga.language.toUTC
import org.jooq.AggregateFunction
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.impl.DSL
import org.jooq.impl.DSL.falseCondition
import org.jooq.impl.DSL.inline
import org.jooq.impl.DSL.noCondition
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.net.URL

@Component
class BookDtoDao(
  private val dsl: DSLContext,
  private val luceneHelper: LuceneHelper,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
  private val transactionTemplate: TransactionTemplate,
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

  private val sorts =
    mapOf(
      "name" to b.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
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
      "metadata.title" to d.TITLE.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
      "metadata.numberSort" to d.NUMBER_SORT,
      "metadata.releaseDate" to d.RELEASE_DATE,
      "readProgress.lastModified" to r.LAST_MODIFIED_DATE,
      "readProgress.readDate" to r.READ_DATE,
      "readList.number" to rlb.NUMBER,
    )

  override fun findAll(
    search: BookSearchWithReadProgress,
    userId: String,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<BookDto> {
    val conditions = search.toCondition().and(restrictions.toCondition(dsl))

    return findAll(conditions, userId, pageable, false, null, search.searchTerm)
  }

  override fun findAllByReadListId(
    readListId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    search: BookSearchWithReadProgress,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<BookDto> {
    val conditions = rlb.READLIST_ID.eq(readListId).and(search.toCondition()).and(restrictions.toCondition(dsl))

    return findAll(conditions, userId, pageable, true, filterOnLibraryIds, search.searchTerm)
  }

  private fun findAll(
    conditions: Condition,
    userId: String,
    pageable: Pageable,
    selectReadListNumber: Boolean = false,
    filterOnLibraryIds: Collection<String>?,
    searchTerm: String?,
  ): Page<BookDto> {
    val bookIds = luceneHelper.searchEntitiesIds(searchTerm, LuceneEntity.Book)

    val searchCondition =
      when {
        bookIds == null -> noCondition()
        bookIds.isEmpty() -> falseCondition()
        // use temp table in case there are many search results
        else -> b.ID.`in`(dsl.selectTempStrings())
      }

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !bookIds.isNullOrEmpty())
          b.ID.sortByValues(bookIds, it.isAscending)
        else
          it.toSortField(sorts)
      }

    val (count, dtos) =
      transactionTemplate.execute {
        // only insert if we know we are going to select on that condition
        if (!bookIds.isNullOrEmpty()) dsl.insertTempStrings(batchSize, bookIds)

        val count =
          dsl.fetchCount(
            dsl.select(b.ID)
              .from(b)
              .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
              .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
              .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
              .leftJoin(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
              .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
              .apply { if (selectReadListNumber) leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID)) }
              .where(conditions)
              .and(searchCondition)
              .groupBy(b.ID),
          )

        val dtos =
          selectBase(userId, selectReadListNumber)
            .where(conditions)
            .and(searchCondition)
            .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
            .orderBy(orderBy)
            .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
            .fetchAndMap()

        count to dtos
      }!!

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

  override fun findByIdOrNull(
    bookId: String,
    userId: String,
  ): BookDto? =
    selectBase(userId)
      .where(b.ID.eq(bookId))
      .fetchAndMap()
      .firstOrNull()

  override fun findPreviousInSeriesOrNull(
    bookId: String,
    userId: String,
  ): BookDto? =
    findSiblingSeries(bookId, userId, next = false)

  override fun findNextInSeriesOrNull(
    bookId: String,
    userId: String,
  ): BookDto? =
    findSiblingSeries(bookId, userId, next = true)

  override fun findPreviousInReadListOrNull(
    readList: ReadList,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): BookDto? =
    findSiblingReadList(readList, bookId, userId, filterOnLibraryIds, restrictions, next = false)

  override fun findNextInReadListOrNull(
    readList: ReadList,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): BookDto? =
    findSiblingReadList(readList, bookId, userId, filterOnLibraryIds, restrictions, next = true)

  override fun findAllOnDeck(
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<BookDto> {
    val seriesIds =
      dsl.select(s.ID)
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

    val dtos =
      seriesIds
        .drop(pageable.pageNumber * pageable.pageSize)
        .take(pageable.pageSize)
        .mapNotNull { seriesId ->
          selectBase(userId)
            .where(b.SERIES_ID.eq(seriesId))
            .and(r.COMPLETED.isNull)
            .orderBy(d.NUMBER_SORT.asc())
            .limit(1)
            .fetchAndMap()
            .firstOrNull()
        }

    return PageImpl(
      dtos,
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageable.sort),
      seriesIds.size.toLong(),
    )
  }

  override fun findAllDuplicates(
    userId: String,
    pageable: Pageable,
  ): Page<BookDto> {
    val hashes =
      dsl.select(b.FILE_HASH, DSL.count(b.ID))
        .from(b)
        .where(b.FILE_HASH.ne(""))
        .groupBy(b.FILE_HASH, b.FILE_SIZE)
        .having(DSL.count(b.ID).gt(1))
        .fetch()
        .associate { it.value1() to it.value2() }

    val count = hashes.values.sum()

    val orderBy = pageable.sort.toOrderBy(sorts)
    val dtos =
      selectBase(userId)
        .where(b.FILE_HASH.`in`(hashes.keys))
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

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId)

  private fun findSiblingSeries(
    bookId: String,
    userId: String,
    next: Boolean,
  ): BookDto? {
    val record =
      dsl.select(b.SERIES_ID, d.NUMBER_SORT)
        .from(b)
        .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
        .where(b.ID.eq(bookId))
        .fetchOne()!!
    val seriesId = record.get(0, String::class.java)
    val numberSort = record.get(1, Float::class.java)

    return selectBase(userId)
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(d.NUMBER_SORT.let { if (next) it.asc() else it.desc() })
      .seek(numberSort)
      .limit(1)
      .fetchAndMap()
      .firstOrNull()
  }

  private fun findSiblingReadList(
    readList: ReadList,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
    next: Boolean,
  ): BookDto? {
    if (readList.ordered) {
      val numberSort =
        dsl.select(rlb.NUMBER)
          .from(b)
          .leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID))
          .where(b.ID.eq(bookId))
          .and(rlb.READLIST_ID.eq(readList.id))
          .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
          .fetchOne(rlb.NUMBER)

      return selectBase(userId, true)
        .where(rlb.READLIST_ID.eq(readList.id))
        .apply { if (restrictions.isRestricted) and(restrictions.toCondition(dsl)) }
        .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
        .orderBy(rlb.NUMBER.let { if (next) it.asc() else it.desc() })
        .seek(numberSort)
        .limit(1)
        .fetchAndMap()
        .firstOrNull()
    } else {
      // it is too complex to perform a seek by release date as it could be null and could also have multiple occurrences of the same value
      // instead we pull the whole list of ids, and perform the seek on the list
      val bookIds =
        dsl.select(b.ID)
          .from(b)
          .leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID))
          .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
          .apply { if (restrictions.isRestricted) leftJoin(sd).on(sd.SERIES_ID.eq(b.SERIES_ID)) }
          .where(rlb.READLIST_ID.eq(readList.id))
          .apply { if (restrictions.isRestricted) and(restrictions.toCondition(dsl)) }
          .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
          .orderBy(d.RELEASE_DATE)
          .fetch(b.ID)

      val bookIndex = bookIds.indexOfFirst { it == bookId }
      if (bookIndex == -1) return null
      val siblingId = bookIds.getOrNull(bookIndex + if (next) 1 else -1) ?: return null

      return selectBase(userId)
        .where(b.ID.eq(siblingId))
        .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
        .limit(1)
        .fetchAndMap()
        .firstOrNull()
    }
  }

  private fun selectBase(
    userId: String,
    selectReadListNumber: Boolean = false,
  ) =
    dsl.select(
      *b.fields(),
      *m.fields(),
      *d.fields(),
      *r.fields(),
      sd.TITLE,
    ).apply { if (selectReadListNumber) select(rlb.NUMBER) }
      .from(b)
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(readProgressCondition(userId))
      .leftJoin(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
      .apply { if (selectReadListNumber) leftJoin(rlb).on(b.ID.eq(rlb.BOOK_ID)) }

  private fun ResultQuery<Record>.fetchAndMap(): MutableList<BookDto> {
    val records = fetch()
    val bookIds = records.getValues(b.ID)

    lateinit var authors: Map<String, List<AuthorDto>>
    lateinit var tags: Map<String, List<String>>
    lateinit var links: Map<String, List<WebLinkDto>>
    transactionTemplate.executeWithoutResult {
      dsl.insertTempStrings(batchSize, bookIds)
      authors =
        dsl.selectFrom(a)
          .where(a.BOOK_ID.`in`(dsl.selectTempStrings()))
          .filter { it.name != null }
          .groupBy({ it.bookId }, { AuthorDto(it.name, it.role) })

      tags =
        dsl.selectFrom(bt)
          .where(bt.BOOK_ID.`in`(dsl.selectTempStrings()))
          .groupBy({ it.bookId }, { it.tag })

      links =
        dsl.selectFrom(bl)
          .where(bl.BOOK_ID.`in`(dsl.selectTempStrings()))
          .groupBy({ it.bookId }, { WebLinkDto(it.label, it.url) })
    }

    return records
      .map { rec ->
        val br = rec.into(b)
        val mr = rec.into(m)
        val dr = rec.into(d)
        val rr = rec.into(r)
        val seriesTitle = rec.into(sd.TITLE).component1()

        br.toDto(mr.toDto(), dr.toDto(authors[br.id].orEmpty(), tags[br.id].orEmpty().toSet(), links[br.id].orEmpty()), if (rr.userId != null) rr.toDto() else null, seriesTitle)
      }
  }

  private fun BookSearchWithReadProgress.toCondition(): Condition {
    var c: Condition = noCondition()

    if (libraryIds != null) c = c.and(b.LIBRARY_ID.`in`(libraryIds))
    if (!seriesIds.isNullOrEmpty()) c = c.and(b.SERIES_ID.`in`(seriesIds))
    if (!mediaStatus.isNullOrEmpty()) c = c.and(m.STATUS.`in`(mediaStatus))
    if (deleted == true) c = c.and(b.DELETED_DATE.isNotNull)
    if (deleted == false) c = c.and(b.DELETED_DATE.isNull)
    if (releasedAfter != null) c = c.and(d.RELEASE_DATE.gt(releasedAfter))
    if (!tags.isNullOrEmpty()) c = c.and(b.ID.`in`(dsl.select(bt.BOOK_ID).from(bt).where(bt.TAG.collate(SqliteUdfDataSource.COLLATION_UNICODE_3).`in`(tags))))

    if (readStatus != null) {
      val cr =
        readStatus.map {
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
        ca = ca.or(b.ID.`in`(dsl.select(a.BOOK_ID).from(a).where(a.NAME.equalIgnoreCase(it.name).and(a.ROLE.equalIgnoreCase(it.role)))))
      }
      c = c.and(ca)
    }

    return c
  }

  private fun BookRecord.toDto(
    media: MediaDto,
    metadata: BookMetadataDto,
    readProgress: ReadProgressDto?,
    seriesTitle: String,
  ) =
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
      fileLastModified = fileLastModified.toUTC(),
      sizeBytes = fileSize,
      media = media,
      metadata = metadata,
      readProgress = readProgress,
      deleted = deletedDate != null,
      fileHash = fileHash,
      oneshot = oneshot,
    )

  private fun MediaRecord.toDto() =
    MediaDto(
      status = status,
      mediaType = mediaType ?: "",
      pagesCount = pageCount.toInt(),
      comment = comment ?: "",
      epubDivinaCompatible = epubDivinaCompatible,
    )

  private fun BookMetadataRecord.toDto(
    authors: List<AuthorDto>,
    tags: Set<String>,
    links: List<WebLinkDto>,
  ) =
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
      deviceId = deviceId,
      deviceName = deviceName,
    )
}
