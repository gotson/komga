package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.BookRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class BookDao(
  private val dsl: DSLContext
) : BookRepository {

  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA

  private val sorts = mapOf(
    "createdDate" to b.CREATED_DATE,
    "seriesId" to b.SERIES_ID,
    "number" to b.NUMBER,
  )

  override fun findByIdOrNull(bookId: String): Book? =
    findByIdOrNull(dsl, bookId)

  private fun findByIdOrNull(dsl: DSLContext, bookId: String): Book? =
    dsl.selectFrom(b)
      .where(b.ID.eq(bookId))
      .fetchOneInto(b)
      ?.toDomain()

  override fun findBySeriesId(seriesId: String): Collection<Book> =
    dsl.selectFrom(b)
      .where(b.SERIES_ID.eq(seriesId))
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAll(): Collection<Book> =
    dsl.select(*b.fields())
      .from(b)
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAll(bookSearch: BookSearch): Collection<Book> =
    dsl.select(*b.fields())
      .from(b)
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .where(bookSearch.toCondition())
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAll(bookSearch: BookSearch, pageable: Pageable): Page<Book> {
    val conditions = bookSearch.toCondition()

    val count = dsl.selectCount()
      .from(b)
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .where(conditions)
      .fetchOne(0, Long::class.java) ?: 0

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items = dsl.select(*b.fields())
      .from(b)
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(b)
      .map { it.toDomain() }

    val pageSort = if (orderBy.size > 1) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count.toInt(), 20), pageSort),
      count
    )
  }

  override fun getLibraryId(bookId: String): String? =
    dsl.select(b.LIBRARY_ID)
      .from(b)
      .where(b.ID.eq(bookId))
      .fetchOne(b.LIBRARY_ID)

  override fun findFirstIdInSeries(seriesId: String): String? =
    dsl.select(b.ID)
      .from(b)
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(d.NUMBER_SORT)
      .limit(1)
      .fetchOne(b.ID)

  override fun findAllIdBySeriesId(seriesId: String): Collection<String> =
    dsl.select(b.ID)
      .from(b)
      .where(b.SERIES_ID.eq(seriesId))
      .fetch(b.ID)

  override fun findAllIdBySeriesIds(seriesIds: Collection<String>): Collection<String> =
    dsl.select(b.ID)
      .from(b)
      .where(b.SERIES_ID.`in`(seriesIds))
      .fetch(0, String::class.java)

  override fun findAllIdByLibraryId(libraryId: String): Collection<String> =
    dsl.select(b.ID)
      .from(b)
      .where(b.LIBRARY_ID.eq(libraryId))
      .fetch(b.ID)

  override fun findAllId(bookSearch: BookSearch, sort: Sort): Collection<String> {
    val conditions = bookSearch.toCondition()

    val orderBy = sort.toOrderBy(sorts)

    return dsl.select(b.ID)
      .from(b)
      .leftJoin(m).on(b.ID.eq(m.BOOK_ID))
      .leftJoin(d).on(b.ID.eq(d.BOOK_ID))
      .where(conditions)
      .orderBy(orderBy)
      .fetch(b.ID)
  }

  override fun insert(book: Book) {
    insertMany(listOf(book))
  }

  override fun insertMany(books: Collection<Book>) {
    if (books.isNotEmpty()) {
      dsl.transaction { config ->
        config.dsl().batch(
          config.dsl().insertInto(
            b,
            b.ID,
            b.NAME,
            b.URL,
            b.NUMBER,
            b.FILE_LAST_MODIFIED,
            b.FILE_SIZE,
            b.LIBRARY_ID,
            b.SERIES_ID
          ).values(null as String?, null, null, null, null, null, null, null)
        ).also { step ->
          books.forEach {
            step.bind(
              it.id,
              it.name,
              it.url,
              it.number,
              it.fileLastModified,
              it.fileSize,
              it.libraryId,
              it.seriesId
            )
          }
        }.execute()
      }
    }
  }

  override fun update(book: Book) {
    update(dsl, book)
  }

  override fun updateMany(books: Collection<Book>) {
    dsl.transaction { config ->
      books.map { update(config.dsl(), it) }
    }
  }

  private fun update(dsl: DSLContext, book: Book) {
    dsl.update(b)
      .set(b.NAME, book.name)
      .set(b.URL, book.url.toString())
      .set(b.NUMBER, book.number)
      .set(b.FILE_LAST_MODIFIED, book.fileLastModified)
      .set(b.FILE_SIZE, book.fileSize)
      .set(b.LIBRARY_ID, book.libraryId)
      .set(b.SERIES_ID, book.seriesId)
      .set(b.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(b.ID.eq(book.id))
      .execute()
  }

  override fun delete(bookId: String) {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(b).where(b.ID.eq(bookId)).execute()
      }
    }
  }

  override fun deleteByBookIds(bookIds: Collection<String>) {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(b).where(b.ID.`in`(bookIds)).execute()
      }
    }
  }

  override fun deleteAll() {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(b).execute()
      }
    }
  }

  override fun count(): Long = dsl.fetchCount(b).toLong()

  private fun BookSearch.toCondition(): Condition {
    var c: Condition = DSL.trueCondition()

    if (!libraryIds.isNullOrEmpty()) c = c.and(b.LIBRARY_ID.`in`(libraryIds))
    if (!seriesIds.isNullOrEmpty()) c = c.and(b.SERIES_ID.`in`(seriesIds))
    searchTerm?.let { c = c.and(d.TITLE.containsIgnoreCase(it)) }
    if (!mediaStatus.isNullOrEmpty()) c = c.and(m.STATUS.`in`(mediaStatus))

    return c
  }

  private fun BookRecord.toDomain() =
    Book(
      name = name,
      url = URL(url),
      fileLastModified = fileLastModified,
      fileSize = fileSize,
      id = id,
      libraryId = libraryId,
      seriesId = seriesId,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      number = number
    )
}
