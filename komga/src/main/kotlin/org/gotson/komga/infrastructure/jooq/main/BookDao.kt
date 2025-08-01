package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.infrastructure.jooq.BookSearchHelper
import org.gotson.komga.infrastructure.jooq.RequiredJoin
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.infrastructure.jooq.rlbAlias
import org.gotson.komga.infrastructure.jooq.toOrderBy
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.unsorted
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class BookDao(
  private val dslRW: DSLContext,
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : BookRepository {
  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val sd = Tables.SERIES_METADATA
  private val r = Tables.READ_PROGRESS

  private val sorts =
    mapOf(
      "createdDate" to b.CREATED_DATE,
      "seriesId" to b.SERIES_ID,
      "number" to b.NUMBER,
    )

  override fun findByIdOrNull(bookId: String): Book? =
    dslRO
      .selectFrom(b)
      .where(b.ID.eq(bookId))
      .fetchOneInto(b)
      ?.toDomain()

  override fun findNotDeletedByLibraryIdAndUrlOrNull(
    libraryId: String,
    url: URL,
  ): Book? =
    dslRO
      .selectFrom(b)
      .where(b.LIBRARY_ID.eq(libraryId).and(b.URL.eq(url.toString())))
      .and(b.DELETED_DATE.isNull)
      .orderBy(b.LAST_MODIFIED_DATE.desc())
      .fetchInto(b)
      .firstOrNull()
      ?.toDomain()

  override fun findAllBySeriesId(seriesId: String): Collection<Book> =
    dslRO
      .selectFrom(b)
      .where(b.SERIES_ID.eq(seriesId))
      .fetchInto(b)
      .map { it.toDomain() }

  @Transactional
  override fun findAllBySeriesIds(seriesIds: Collection<String>): Collection<Book> {
    dslRO.withTempTable(batchSize, seriesIds).use { tempTable ->
      return dslRO
        .selectFrom(b)
        .where(b.SERIES_ID.`in`(tempTable.selectTempStrings()))
        .fetchInto(b)
        .map { it.toDomain() }
    }
  }

  @Transactional
  override fun findAllNotDeletedByLibraryIdAndUrlNotIn(
    libraryId: String,
    urls: Collection<URL>,
  ): Collection<Book> {
    dslRO.withTempTable(batchSize, urls.map { it.toString() }).use { tempTable ->

      return dslRO
        .selectFrom(b)
        .where(b.LIBRARY_ID.eq(libraryId))
        .and(b.DELETED_DATE.isNull)
        .and(b.URL.notIn(tempTable.selectTempStrings()))
        .fetchInto(b)
        .map { it.toDomain() }
    }
  }

  override fun findAllDeletedByFileSize(fileSize: Long): Collection<Book> =
    dslRO
      .selectFrom(b)
      .where(b.DELETED_DATE.isNotNull.and(b.FILE_SIZE.eq(fileSize)))
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAll(): Collection<Book> =
    dslRO
      .selectFrom(b)
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAll(
    searchCondition: SearchCondition.Book?,
    searchContext: SearchContext,
    pageable: Pageable,
  ): Page<Book> {
    val bookCondition = BookSearchHelper(searchContext).toCondition(searchCondition)

    val count =
      dslRO
        .selectCount()
        .from(b)
        .apply {
          bookCondition.second.forEach { join ->
            when (join) {
              RequiredJoin.BookMetadata -> innerJoin(d).on(b.ID.eq(d.BOOK_ID))
              RequiredJoin.SeriesMetadata -> innerJoin(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
              RequiredJoin.Media -> innerJoin(m).on(b.ID.eq(m.BOOK_ID))
              is RequiredJoin.ReadProgress -> leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(r.USER_ID.eq(join.userId))
              is RequiredJoin.ReadList -> {
                val rlbAlias = rlbAlias(join.readListId)
                leftJoin(rlbAlias).on(rlbAlias.BOOK_ID.eq(b.ID).and(rlbAlias.READLIST_ID.eq(join.readListId)))
              }
              // shouldn't be required for books
              RequiredJoin.BookMetadataAggregation -> Unit
              is RequiredJoin.Collection -> Unit
            }
          }
        }.where(bookCondition.first)
        .fetchOne(0, Long::class.java) ?: 0

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items =
      dslRO
        .select(*b.fields())
        .from(b)
        .apply {
          bookCondition.second.forEach { join ->
            when (join) {
              RequiredJoin.BookMetadata -> innerJoin(d).on(b.ID.eq(d.BOOK_ID))
              RequiredJoin.SeriesMetadata -> innerJoin(sd).on(b.SERIES_ID.eq(sd.SERIES_ID))
              RequiredJoin.Media -> innerJoin(m).on(b.ID.eq(m.BOOK_ID))
              is RequiredJoin.ReadProgress -> leftJoin(r).on(b.ID.eq(r.BOOK_ID)).and(r.USER_ID.eq(join.userId))
              is RequiredJoin.ReadList -> {
                val rlbAlias = rlbAlias(join.readListId)
                leftJoin(rlbAlias).on(rlbAlias.BOOK_ID.eq(b.ID).and(rlbAlias.READLIST_ID.eq(join.readListId)))
              }
              // shouldn't be required for books
              RequiredJoin.BookMetadataAggregation -> Unit
              is RequiredJoin.Collection -> Unit
            }
          }
        }.where(bookCondition.first)
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(b)
        .map { it.toDomain() }
    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else unsorted()

    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count.toInt(), 20), pageSort),
      count,
    )
  }

  override fun getLibraryIdOrNull(bookId: String): String? =
    dslRO
      .select(b.LIBRARY_ID)
      .from(b)
      .where(b.ID.eq(bookId))
      .fetchOne(b.LIBRARY_ID)

  override fun getSeriesIdOrNull(bookId: String): String? =
    dslRO
      .select(b.SERIES_ID)
      .from(b)
      .where(b.ID.eq(bookId))
      .fetchOne(b.SERIES_ID)

  override fun findFirstIdInSeriesOrNull(seriesId: String): String? =
    dslRO
      .select(b.ID)
      .from(b)
      .leftJoin(d)
      .on(b.ID.eq(d.BOOK_ID))
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(d.NUMBER_SORT)
      .limit(1)
      .fetchOne(b.ID)

  override fun findLastIdInSeriesOrNull(seriesId: String): String? =
    dslRO
      .select(b.ID)
      .from(b)
      .leftJoin(d)
      .on(b.ID.eq(d.BOOK_ID))
      .where(b.SERIES_ID.eq(seriesId))
      .orderBy(d.NUMBER_SORT.desc())
      .limit(1)
      .fetchOne(b.ID)

  override fun findFirstUnreadIdInSeriesOrNull(
    seriesId: String,
    userId: String,
  ): String? =
    dslRO
      .select(b.ID)
      .from(b)
      .leftJoin(d)
      .on(b.ID.eq(d.BOOK_ID))
      .leftJoin(r)
      .on(b.ID.eq(r.BOOK_ID))
      .and(r.USER_ID.eq(userId).or(r.USER_ID.isNull))
      .where(b.SERIES_ID.eq(seriesId))
      .and(r.COMPLETED.isNull.or(r.COMPLETED.isFalse))
      .orderBy(d.NUMBER_SORT)
      .limit(1)
      .fetchOne(b.ID)

  override fun findAllIdsBySeriesId(seriesId: String): Collection<String> =
    dslRO
      .select(b.ID)
      .from(b)
      .where(b.SERIES_ID.eq(seriesId))
      .fetch(b.ID)

  override fun findAllIdsByLibraryId(libraryId: String): Collection<String> =
    dslRO
      .select(b.ID)
      .from(b)
      .where(b.LIBRARY_ID.eq(libraryId))
      .fetch(b.ID)

  override fun existsById(bookId: String): Boolean = dslRO.fetchExists(b, b.ID.eq(bookId))

  override fun findAllByLibraryIdAndMediaTypes(
    libraryId: String,
    mediaTypes: Collection<String>,
  ): Collection<Book> =
    dslRO
      .select(*b.fields())
      .from(b)
      .leftJoin(m)
      .on(b.ID.eq(m.BOOK_ID))
      .where(b.LIBRARY_ID.eq(libraryId))
      .and(m.MEDIA_TYPE.`in`(mediaTypes))
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAllByLibraryIdAndMismatchedExtension(
    libraryId: String,
    mediaType: String,
    extension: String,
  ): Collection<Book> =
    dslRO
      .select(*b.fields())
      .from(b)
      .leftJoin(m)
      .on(b.ID.eq(m.BOOK_ID))
      .where(b.LIBRARY_ID.eq(libraryId))
      .and(m.MEDIA_TYPE.eq(mediaType))
      .and(b.URL.notLike("%.$extension"))
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAllByLibraryIdAndWithEmptyHash(libraryId: String): Collection<Book> =
    dslRO
      .selectFrom(b)
      .where(b.LIBRARY_ID.eq(libraryId))
      .and(b.FILE_HASH.eq(""))
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAllByLibraryIdAndWithEmptyHashKoreader(libraryId: String): Collection<Book> =
    dslRO
      .selectFrom(b)
      .where(b.LIBRARY_ID.eq(libraryId))
      .and(b.FILE_HASH_KOREADER.eq(""))
      .fetchInto(b)
      .map { it.toDomain() }

  override fun findAllByHashKoreader(hashKoreader: String): Collection<Book> =
    dslRO
      .selectFrom(b)
      .where(b.FILE_HASH_KOREADER.eq(hashKoreader))
      .fetchInto(b)
      .map { it.toDomain() }

  @Transactional
  override fun insert(book: Book) {
    insert(listOf(book))
  }

  @Transactional
  override fun insert(books: Collection<Book>) {
    if (books.isNotEmpty()) {
      books.chunked(batchSize).forEach { chunk ->
        dslRW
          .batch(
            dslRW
              .insertInto(
                b,
                b.ID,
                b.NAME,
                b.URL,
                b.NUMBER,
                b.FILE_LAST_MODIFIED,
                b.FILE_SIZE,
                b.FILE_HASH,
                b.FILE_HASH_KOREADER,
                b.LIBRARY_ID,
                b.SERIES_ID,
                b.DELETED_DATE,
                b.ONESHOT,
              ).values(null as String?, null, null, null, null, null, null, null, null, null, null, null),
          ).also { step ->
            chunk.forEach {
              step.bind(
                it.id,
                it.name,
                it.url,
                it.number,
                it.fileLastModified,
                it.fileSize,
                it.fileHash,
                it.fileHashKoreader,
                it.libraryId,
                it.seriesId,
                it.deletedDate,
                it.oneshot,
              )
            }
          }.execute()
      }
    }
  }

  @Transactional
  override fun update(book: Book) {
    updateBook(book)
  }

  @Transactional
  override fun update(books: Collection<Book>) {
    books.map { updateBook(it) }
  }

  private fun updateBook(book: Book) {
    dslRW
      .update(b)
      .set(b.NAME, book.name)
      .set(b.URL, book.url.toString())
      .set(b.NUMBER, book.number)
      .set(b.FILE_LAST_MODIFIED, book.fileLastModified)
      .set(b.FILE_SIZE, book.fileSize)
      .set(b.FILE_HASH, book.fileHash)
      .set(b.FILE_HASH_KOREADER, book.fileHashKoreader)
      .set(b.LIBRARY_ID, book.libraryId)
      .set(b.SERIES_ID, book.seriesId)
      .set(b.DELETED_DATE, book.deletedDate)
      .set(b.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .set(b.ONESHOT, book.oneshot)
      .where(b.ID.eq(book.id))
      .execute()
  }

  override fun delete(bookId: String) {
    dslRW.deleteFrom(b).where(b.ID.eq(bookId)).execute()
  }

  @Transactional
  override fun delete(bookIds: Collection<String>) {
    dslRW.withTempTable(batchSize, bookIds).use { tempTable ->
      dslRW.deleteFrom(b).where(b.ID.`in`(tempTable.selectTempStrings())).execute()
    }
  }

  override fun deleteAll() {
    dslRW.deleteFrom(b).execute()
  }

  override fun count(): Long = dslRO.fetchCount(b).toLong()

  override fun countGroupedByLibraryId(): Map<String, Int> =
    dslRO
      .select(b.LIBRARY_ID, DSL.count(b.ID))
      .from(b)
      .groupBy(b.LIBRARY_ID)
      .fetchMap(b.LIBRARY_ID, DSL.count(b.ID))

  override fun getFilesizeGroupedByLibraryId(): Map<String, BigDecimal> =
    dslRO
      .select(b.LIBRARY_ID, DSL.sum(b.FILE_SIZE))
      .from(b)
      .groupBy(b.LIBRARY_ID)
      .fetchMap(b.LIBRARY_ID, DSL.sum(b.FILE_SIZE))

  private fun BookRecord.toDomain() =
    Book(
      name = name,
      url = URL(url),
      fileLastModified = fileLastModified,
      fileSize = fileSize,
      fileHash = fileHash,
      fileHashKoreader = fileHashKoreader,
      id = id,
      libraryId = libraryId,
      seriesId = seriesId,
      deletedDate = deletedDate,
      oneshot = oneshot,
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
      number = number,
    )
}
