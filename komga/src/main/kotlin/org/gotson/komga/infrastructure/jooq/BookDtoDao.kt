package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.interfaces.rest.dto.AuthorDto
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.gotson.komga.interfaces.rest.dto.BookMetadataDto
import org.gotson.komga.interfaces.rest.dto.MediaDto
import org.gotson.komga.interfaces.rest.persistence.BookDtoRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.BookMetadataAuthorRecord
import org.gotson.komga.jooq.tables.records.BookMetadataRecord
import org.gotson.komga.jooq.tables.records.BookRecord
import org.gotson.komga.jooq.tables.records.MediaRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.net.URL

@Component
class BookDtoDao(
  private val dsl: DSLContext
) : BookDtoRepository {

  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val a = Tables.BOOK_METADATA_AUTHOR

  private val mediaFields = m.fields().filterNot { it.name == m.THUMBNAIL.name }.toTypedArray()

  private val sorts = mapOf(
    "metadata.numberSort" to d.NUMBER_SORT,
    "createdDate" to b.CREATED_DATE,
    "lastModifiedDate" to b.LAST_MODIFIED_DATE,
    "fileSize" to b.FILE_SIZE
  )

  override fun findAll(search: BookSearch, pageable: Pageable): Page<BookDto> {
    val conditions = search.toCondition()

    val count = dsl.selectCount()
      .from(b)
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .where(conditions)
      .fetchOne(0, Long::class.java)

    val orderBy = pageable.sort.toOrderBy(sorts)

    val dtos = selectBase()
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

  override fun findByIdOrNull(bookId: Long): BookDto? =
    selectBase()
      .where(b.ID.eq(bookId))
      .fetchAndMap()
      .firstOrNull()

  override fun findPreviousInSeries(bookId: Long): BookDto? = findSibling(bookId, next = false)

  override fun findNextInSeries(bookId: Long): BookDto? = findSibling(bookId, next = true)


  private fun findSibling(bookId: Long, next: Boolean): BookDto? {
    val record = dsl.select(b.SERIES_ID, d.NUMBER_SORT)
      .from(b)
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .where(b.ID.eq(bookId))
      .fetchOne()
    val seriesId = record.get(0, Long::class.java)
    val numberSort = record.get(1, Float::class.java)

    return selectBase()
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(d.NUMBER_SORT.let { if (next) it.asc() else it.desc() })
      .seek(numberSort)
      .limit(1)
      .fetchAndMap()
      .firstOrNull()
  }

  private fun selectBase() =
    dsl.select(
      *b.fields(),
      *mediaFields,
      *d.fields(),
      *a.fields()
    ).from(b)
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(a).on(d.BOOK_ID.eq(a.BOOK_ID))

  private fun ResultQuery<Record>.fetchAndMap() =
    fetchGroups(
      { it.into(*b.fields(), *mediaFields, *d.fields()) }, { it.into(a) }
    ).map { (r, ar) ->
      val br = r.into(b)
      val mr = r.into(m)
      val dr = r.into(d)
      br.toDto(mr.toDto(), dr.toDto(ar))
    }

  private fun BookSearch.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    if (libraryIds.isNotEmpty()) c = c.and(b.LIBRARY_ID.`in`(libraryIds))
    if (seriesIds.isNotEmpty()) c = c.and(b.SERIES_ID.`in`(seriesIds))
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(it)) }
    if (mediaStatus.isNotEmpty()) c = c.and(m.STATUS.`in`(mediaStatus))

    return c
  }

  private fun BookRecord.toDto(media: MediaDto, metadata: BookMetadataDto) =
    BookDto(
      id = id,
      seriesId = seriesId,
      libraryId = libraryId,
      name = name,
      url = URL(url).toURI().path,
      number = number,
      created = createdDate.toUTC(),
      lastModified = lastModifiedDate.toUTC(),
      fileLastModified = fileLastModified.toUTC(),
      sizeBytes = fileSize,
      media = media,
      metadata = metadata
    )

  private fun MediaRecord.toDto() =
    MediaDto(
      status = status,
      mediaType = mediaType ?: "",
      pagesCount = pageCount.toInt(),
      comment = comment ?: ""
    )

  private fun BookMetadataRecord.toDto(ar: Collection<BookMetadataAuthorRecord>) =
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
      authors = ar.filter { it.name != null }.map { AuthorDto(it.name, it.role) },
      authorsLock = authorsLock
    )
}
