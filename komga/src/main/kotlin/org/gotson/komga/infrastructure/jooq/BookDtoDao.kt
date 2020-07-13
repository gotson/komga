package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.interfaces.rest.dto.AuthorDto
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.gotson.komga.interfaces.rest.dto.BookMetadataDto
import org.gotson.komga.interfaces.rest.dto.MediaDto
import org.gotson.komga.interfaces.rest.dto.ReadProgressDto
import org.gotson.komga.interfaces.rest.persistence.BookDtoRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.BookMetadataRecord
import org.gotson.komga.jooq.tables.records.BookRecord
import org.gotson.komga.jooq.tables.records.MediaRecord
import org.gotson.komga.jooq.tables.records.ReadProgressRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.impl.DSL
import org.jooq.impl.DSL.inline
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import toFilePath
import java.net.URL

@Component
class BookDtoDao(
  private val dsl: DSLContext
) : BookDtoRepository {

  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val r = Tables.READ_PROGRESS
  private val a = Tables.BOOK_METADATA_AUTHOR
  private val s = Tables.SERIES

  private val mediaFields = m.fields().filterNot { it.name == m.THUMBNAIL.name }.toTypedArray()

  private val sorts = mapOf(
    "name" to DSL.lower(b.NAME),
    "created" to b.CREATED_DATE,
    "createdDate" to b.CREATED_DATE,
    "lastModified" to b.LAST_MODIFIED_DATE,
    "lastModifiedDate" to b.LAST_MODIFIED_DATE,
    "fileSize" to b.FILE_SIZE,
    "url" to DSL.lower(b.URL),
    "media.status" to DSL.lower(m.STATUS),
    "media.comment" to DSL.lower(m.COMMENT),
    "media.mediaType" to DSL.lower(m.MEDIA_TYPE),
    "metadata.numberSort" to d.NUMBER_SORT,
    "readProgress.lastModified" to r.LAST_MODIFIED_DATE
  )

  override fun findAll(search: BookSearchWithReadProgress, userId: String, pageable: Pageable): Page<BookDto> {
    val conditions = search.toCondition()

    val count = dsl.selectCount()
      .from(b)
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
      .and(readProgressCondition(userId))
      .where(conditions)
      .fetchOne(0, Long::class.java)

    val orderBy = pageable.sort.toOrderBy(sorts)

    val dtos = selectBase(userId)
      .where(conditions)
      .orderBy(orderBy)
      .limit(pageable.pageSize)
      .offset(pageable.offset)
      .fetchAndMap()

    return PageImpl(
      dtos,
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageable.sort),
      count.toLong()
    )
  }

  override fun findByIdOrNull(bookId: String, userId: String): BookDto? =
    selectBase(userId)
      .where(b.ID.eq(bookId))
      .fetchAndMap()
      .firstOrNull()

  override fun findPreviousInSeries(bookId: String, userId: String): BookDto? = findSibling(bookId, userId, next = false)

  override fun findNextInSeries(bookId: String, userId: String): BookDto? = findSibling(bookId, userId, next = true)


  override fun findOnDeck(libraryIds: Collection<String>, userId: String, pageable: Pageable): Page<BookDto> {
    val conditions = if (libraryIds.isEmpty()) DSL.trueCondition() else s.LIBRARY_ID.`in`(libraryIds)

    val seriesIds = dsl.select(s.ID)
      .from(s)
      .leftJoin(b).on(s.ID.eq(b.SERIES_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
      .and(readProgressCondition(userId))
      .where(conditions)
      .groupBy(s.ID)
      .having(SeriesDtoDao.countUnread.ge(inline(1.toBigDecimal())))
      .and(SeriesDtoDao.countRead.ge(inline(1.toBigDecimal())))
      .and(SeriesDtoDao.countInProgress.eq(inline(0.toBigDecimal())))
      .orderBy(DSL.max(r.LAST_MODIFIED_DATE).desc())
      .fetchInto(String::class.java)

    val dtos = seriesIds
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
      seriesIds.size.toLong()
    )
  }

  private fun readProgressCondition(userId: String): Condition = r.USER_ID.eq(userId).or(r.USER_ID.isNull)

  private fun findSibling(bookId: String, userId: String, next: Boolean): BookDto? {
    val record = dsl.select(b.SERIES_ID, d.NUMBER_SORT)
      .from(b)
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .where(b.ID.eq(bookId))
      .fetchOne()
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

  private fun selectBase(userId: String) =
    dsl.select(
      *b.fields(),
      *mediaFields,
      *d.fields(),
      *r.fields()
    ).from(b)
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(r).on(b.ID.eq(r.BOOK_ID))
      .and(readProgressCondition(userId))

  private fun ResultQuery<Record>.fetchAndMap() =
    fetch()
      .map { rec ->
        val br = rec.into(b)
        val mr = rec.into(m)
        val dr = rec.into(d)
        val rr = rec.into(r)

        val authors = dsl.selectFrom(a)
          .where(a.BOOK_ID.eq(br.id))
          .fetchInto(a)
          .filter { it.name != null }
          .map { AuthorDto(it.name, it.role) }

        br.toDto(mr.toDto(), dr.toDto(authors), if (rr.userId != null) rr.toDto() else null)
      }

  private fun BookSearchWithReadProgress.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    libraryIds?.let { c = c.and(b.LIBRARY_ID.`in`(it)) }
    seriesIds?.let { c = c.and(b.SERIES_ID.`in`(it)) }
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(it)) }
    mediaStatus?.let { c = c.and(m.STATUS.`in`(it)) }

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

    return c
  }

  private fun BookRecord.toDto(media: MediaDto, metadata: BookMetadataDto, readProgress: ReadProgressDto?) =
    BookDto(
      id = id,
      seriesId = seriesId,
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
      readProgress = readProgress
    )

  private fun MediaRecord.toDto() =
    MediaDto(
      status = status,
      mediaType = mediaType ?: "",
      pagesCount = pageCount.toInt(),
      comment = comment ?: ""
    )

  private fun BookMetadataRecord.toDto(authors: List<AuthorDto>) =
    BookMetadataDto(
      title = title,
      titleLock = titleLock,
      summary = summary,
      summaryLock = summaryLock,
      number = number,
      numberLock = numberLock,
      numberSort = numberSort,
      numberSortLock = numberSortLock,
      readingDirection = readingDirection ?: "",
      readingDirectionLock = readingDirectionLock,
      publisher = publisher,
      publisherLock = publisherLock,
      ageRating = ageRating,
      ageRatingLock = ageRatingLock,
      releaseDate = releaseDate,
      releaseDateLock = releaseDateLock,
      authors = authors,
      authorsLock = authorsLock
    )

  private fun ReadProgressRecord.toDto() =
    ReadProgressDto(
      page = page,
      completed = completed,
      created = createdDate,
      lastModified = lastModifiedDate
    )
}
