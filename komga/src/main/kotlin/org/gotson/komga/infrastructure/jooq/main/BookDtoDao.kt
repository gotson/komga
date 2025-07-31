package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.infrastructure.datasource.SqliteUdfDataSource
import org.gotson.komga.infrastructure.jooq.BookSearchHelper
import org.gotson.komga.infrastructure.jooq.RequiredJoin
import org.gotson.komga.infrastructure.jooq.TempTable
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.infrastructure.jooq.noCase
import org.gotson.komga.infrastructure.jooq.rlbAlias
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
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.SelectOnConditionStep
import org.jooq.impl.DSL
import org.jooq.impl.DSL.falseCondition
import org.jooq.impl.DSL.noCondition
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.net.URL

@Component
class BookDtoDao(
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
  private val luceneHelper: LuceneHelper,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
  private val bookCommonDao: BookCommonDao,
) : BookDtoRepository {
  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val r = Tables.READ_PROGRESS
  private val a = Tables.BOOK_METADATA_AUTHOR
  private val sd = Tables.SERIES_METADATA
  private val rlb = Tables.READLIST_BOOK
  private val bt = Tables.BOOK_METADATA_TAG
  private val bl = Tables.BOOK_METADATA_LINK

  private val onDeckFields = b.fields() + m.fields() + d.fields() + r.fields() + sd.TITLE

  private val sorts =
    mapOf(
      "name" to b.NAME.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
      "series" to sd.TITLE_SORT.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
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
      "media.pagesCount" to m.PAGE_COUNT,
      "metadata.title" to d.TITLE.collate(SqliteUdfDataSource.COLLATION_UNICODE_3),
      "metadata.numberSort" to d.NUMBER_SORT,
      "metadata.releaseDate" to d.RELEASE_DATE,
      "readProgress.lastModified" to r.LAST_MODIFIED_DATE,
      "readProgress.readDate" to r.READ_DATE,
      "readList.number" to rlb.NUMBER,
    )

  override fun findAll(pageable: Pageable): Page<BookDto> = findAll(BookSearch(), SearchContext.ofAnonymousUser(), pageable)

  override fun findAll(
    context: SearchContext,
    pageable: Pageable,
  ): Page<BookDto> = findAll(BookSearch(), context, pageable)

  override fun findAll(
    search: BookSearch,
    context: SearchContext,
    pageable: Pageable,
  ): Page<BookDto> {
    requireNotNull(context.userId) { "Missing userId in search context" }

    val (conditions, joins) = BookSearchHelper(context).toCondition(search.condition)
    return findAll(conditions, context.userId, pageable, search.fullTextSearch, joins)
  }

  private fun findAll(
    conditions: Condition,
    userId: String,
    pageable: Pageable,
    searchTerm: String?,
    joins: Set<RequiredJoin>,
  ): Page<BookDto> {
    val bookIds = luceneHelper.searchEntitiesIds(searchTerm, LuceneEntity.Book)

    val orderBy =
      pageable.sort.mapNotNull {
        if (it.property == "relevance" && !bookIds.isNullOrEmpty()) {
          b.ID.sortByValues(bookIds, it.isAscending)
        } else {
          if (it.property == "readList.number") {
            val readListId = joins.filterIsInstance<RequiredJoin.ReadList>().firstOrNull()?.readListId ?: return@mapNotNull null
            val f = rlbAlias(readListId).NUMBER
            if (it.isAscending) f.asc() else f.desc()
          } else {
            it.toSortField(sorts)
          }
        }
      }

    // don't use the DSLContext.withTempTable form to control optional creation
    TempTable(dslRO).use { tempTable ->

      val searchCondition =
        when {
          bookIds == null -> noCondition()
          bookIds.isEmpty() -> falseCondition()
          // use temp table in case there are many search results
          else -> {
            tempTable.insertTempStrings(batchSize, bookIds)
            b.ID.`in`(tempTable.selectTempStrings())
          }
        }

      val count =
        dslRO.fetchCount(
          dslRO
            .select(b.ID)
            .from(b)
            .leftJoin(m)
            .on(b.ID.eq(m.BOOK_ID))
            .leftJoin(d)
            .on(b.ID.eq(d.BOOK_ID))
            .leftJoin(r)
            .on(b.ID.eq(r.BOOK_ID))
            .and(readProgressCondition(userId))
            .leftJoin(sd)
            .on(b.SERIES_ID.eq(sd.SERIES_ID))
            .apply {
              joins.forEach { join ->
                when (join) {
                  is RequiredJoin.ReadList -> {
                    val rlbAlias = rlbAlias(join.readListId)
                    leftJoin(rlbAlias).on(rlbAlias.BOOK_ID.eq(b.ID).and(rlbAlias.READLIST_ID.eq(join.readListId)))
                  }
                  // always joined
                  RequiredJoin.BookMetadata -> Unit
                  RequiredJoin.Media -> Unit
                  is RequiredJoin.ReadProgress -> Unit
                  // Series joins - not needed
                  RequiredJoin.BookMetadataAggregation -> Unit
                  RequiredJoin.SeriesMetadata -> Unit
                  is RequiredJoin.Collection -> Unit
                }
              }
            }.where(conditions)
            .and(searchCondition)
            .groupBy(b.ID),
        )

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
  }

  override fun findByIdOrNull(
    bookId: String,
    userId: String,
  ): BookDto? =
    dslRO
      .selectBase(userId)
      .where(b.ID.eq(bookId))
      .fetchAndMap(dslRO)
      .firstOrNull()

  override fun findPreviousInSeriesOrNull(
    bookId: String,
    userId: String,
  ): BookDto? = findSiblingSeries(bookId, userId, next = false)

  override fun findNextInSeriesOrNull(
    bookId: String,
    userId: String,
  ): BookDto? = findSiblingSeries(bookId, userId, next = true)

  override fun findPreviousInReadListOrNull(
    readList: ReadList,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): BookDto? = findSiblingReadList(readList, bookId, userId, filterOnLibraryIds, restrictions, next = false)

  override fun findNextInReadListOrNull(
    readList: ReadList,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions,
  ): BookDto? = findSiblingReadList(readList, bookId, userId, filterOnLibraryIds, restrictions, next = true)

  override fun findAllOnDeck(
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
    restrictions: ContentRestrictions,
  ): Page<BookDto> {
    val (query, sortField, _) = bookCommonDao.getBooksOnDeckQuery(userId, restrictions, filterOnLibraryIds, onDeckFields)

    val count = dslRO.fetchCount(query)
    val dtos =
      query
        .orderBy(sortField.desc())
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchAndMap(dslRO)

    return PageImpl(
      dtos,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.unsorted())
      else
        PageRequest.of(0, maxOf(count, 20), Sort.unsorted()),
      count.toLong(),
    )
  }

  override fun findAllDuplicates(
    userId: String,
    pageable: Pageable,
  ): Page<BookDto> {
    val hashes =
      dslRO
        .select(b.FILE_HASH, DSL.count(b.ID))
        .from(b)
        .where(b.FILE_HASH.ne(""))
        .groupBy(b.FILE_HASH, b.FILE_SIZE)
        .having(DSL.count(b.ID).gt(1))
        .fetch()
        .associate { it.value1() to it.value2() }

    val count = hashes.values.sum()

    val orderBy = pageable.sort.toOrderBy(sorts)
    val dtos =
      dslRO
        .selectBase(userId)
        .where(b.FILE_HASH.`in`(hashes.keys))
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

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId)

  private fun findSiblingSeries(
    bookId: String,
    userId: String,
    next: Boolean,
  ): BookDto? {
    val record =
      dslRO
        .select(b.SERIES_ID, d.NUMBER_SORT)
        .from(b)
        .leftJoin(d)
        .on(b.ID.eq(d.BOOK_ID))
        .where(b.ID.eq(bookId))
        .fetchOne()!!
    val seriesId = record.get(0, String::class.java)
    val numberSort = record.get(1, Float::class.java)

    return dslRO
      .selectBase(userId)
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(d.NUMBER_SORT.let { if (next) it.asc() else it.desc() })
      .seek(numberSort)
      .limit(1)
      .fetchAndMap(dslRO)
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
        dslRO
          .select(rlb.NUMBER)
          .from(b)
          .leftJoin(rlb)
          .on(b.ID.eq(rlb.BOOK_ID))
          .where(b.ID.eq(bookId))
          .and(rlb.READLIST_ID.eq(readList.id))
          .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
          .fetchOne(rlb.NUMBER)

      return dslRO
        .selectBase(userId, setOf(RequiredJoin.ReadList(readList.id)))
        .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
        .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
        .orderBy(rlbAlias(readList.id).NUMBER.let { if (next) it.asc() else it.desc() })
        .seek(numberSort)
        .limit(1)
        .fetchAndMap(dslRO)
        .firstOrNull()
    } else {
      // it is too complex to perform a seek by release date as it could be null and could also have multiple occurrences of the same value
      // instead we pull the whole list of ids, and perform the seek on the list
      val bookIds =
        dslRO
          .select(b.ID)
          .from(b)
          .leftJoin(rlb)
          .on(b.ID.eq(rlb.BOOK_ID))
          .leftJoin(d)
          .on(b.ID.eq(d.BOOK_ID))
          .apply { if (restrictions.isRestricted) leftJoin(sd).on(sd.SERIES_ID.eq(b.SERIES_ID)) }
          .where(rlb.READLIST_ID.eq(readList.id))
          .apply { if (restrictions.isRestricted) and(restrictions.toCondition()) }
          .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
          .orderBy(d.RELEASE_DATE)
          .fetch(b.ID)

      val bookIndex = bookIds.indexOfFirst { it == bookId }
      if (bookIndex == -1) return null
      val siblingId = bookIds.getOrNull(bookIndex + if (next) 1 else -1) ?: return null

      return dslRO
        .selectBase(userId)
        .where(b.ID.eq(siblingId))
        .apply { filterOnLibraryIds?.let { and(b.LIBRARY_ID.`in`(it)) } }
        .limit(1)
        .fetchAndMap(dslRO)
        .firstOrNull()
    }
  }

  private fun DSLContext.selectBase(
    userId: String,
    joins: Set<RequiredJoin> = emptySet(),
  ): SelectOnConditionStep<Record> {
    val selectFields =
      listOf(
        *b.fields(),
        *m.fields(),
        *d.fields(),
        *r.fields(),
        sd.TITLE,
      )

    return this
      .select(selectFields)
      .from(b)
      .leftJoin(m)
      .on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d)
      .on(b.ID.eq(d.BOOK_ID))
      .leftJoin(r)
      .on(b.ID.eq(r.BOOK_ID))
      .and(readProgressCondition(userId))
      .leftJoin(sd)
      .on(b.SERIES_ID.eq(sd.SERIES_ID))
      .apply {
        joins.forEach { join ->
          when (join) {
            is RequiredJoin.ReadList -> {
              val rlbAlias = rlbAlias(join.readListId)
              leftJoin(rlbAlias).on(rlbAlias.BOOK_ID.eq(b.ID).and(rlbAlias.READLIST_ID.eq(join.readListId)))
            }
            // always joined
            RequiredJoin.BookMetadata -> Unit
            RequiredJoin.Media -> Unit
            is RequiredJoin.ReadProgress -> Unit
            // Series joins - not needed
            RequiredJoin.BookMetadataAggregation -> Unit
            RequiredJoin.SeriesMetadata -> Unit
            is RequiredJoin.Collection -> Unit
          }
        }
      }
  }

  private fun ResultQuery<Record>.fetchAndMap(dsl: DSLContext): MutableList<BookDto> {
    val records = fetch()
    val bookIds = records.getValues(b.ID)

    lateinit var authors: Map<String, List<AuthorDto>>
    lateinit var tags: Map<String, List<String>>
    lateinit var links: Map<String, List<WebLinkDto>>
    dsl.withTempTable(batchSize, bookIds).use { tempTable ->
      authors =
        dsl
          .selectFrom(a)
          .where(a.BOOK_ID.`in`(tempTable.selectTempStrings()))
          .filter { it.name != null }
          .groupBy({ it.bookId }, { AuthorDto(it.name, it.role) })

      tags =
        dsl
          .selectFrom(bt)
          .where(bt.BOOK_ID.`in`(tempTable.selectTempStrings()))
          .groupBy({ it.bookId }, { it.tag })

      links =
        dsl
          .selectFrom(bl)
          .where(bl.BOOK_ID.`in`(tempTable.selectTempStrings()))
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

  private fun BookRecord.toDto(
    media: MediaDto,
    metadata: BookMetadataDto,
    readProgress: ReadProgressDto?,
    seriesTitle: String,
  ) = BookDto(
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
      epubIsKepub = epubIsKepub,
    )

  private fun BookMetadataRecord.toDto(
    authors: List<AuthorDto>,
    tags: Set<String>,
    links: List<WebLinkDto>,
  ) = BookMetadataDto(
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
