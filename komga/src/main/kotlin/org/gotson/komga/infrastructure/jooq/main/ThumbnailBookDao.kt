package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.ThumbnailBookRecord
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Component
class ThumbnailBookDao(
  private val dsl: DSLContext,
  @Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : ThumbnailBookRepository {
  private val tb = Tables.THUMBNAIL_BOOK

  override fun findAllByBookId(bookId: String): Collection<ThumbnailBook> =
    dsl
      .selectFrom(tb)
      .where(tb.BOOK_ID.eq(bookId))
      .fetchInto(tb)
      .map { it.toDomain() }

  override fun findAllByBookIdAndType(
    bookId: String,
    type: Set<ThumbnailBook.Type>,
  ): Collection<ThumbnailBook> =
    dsl
      .selectFrom(tb)
      .where(tb.BOOK_ID.eq(bookId))
      .and(tb.TYPE.`in`(type.map { it.name }))
      .fetchInto(tb)
      .map { it.toDomain() }

  override fun findByIdOrNull(thumbnailId: String): ThumbnailBook? =
    dsl
      .selectFrom(tb)
      .where(tb.ID.eq(thumbnailId))
      .fetchOneInto(tb)
      ?.toDomain()

  override fun findSelectedByBookIdOrNull(bookId: String): ThumbnailBook? =
    dsl
      .selectFrom(tb)
      .where(tb.BOOK_ID.eq(bookId))
      .and(tb.SELECTED.isTrue)
      .limit(1)
      .fetchInto(tb)
      .map { it.toDomain() }
      .firstOrNull()

  override fun findAllWithoutMetadata(pageable: Pageable): Page<ThumbnailBook> {
    val query =
      dsl
        .selectFrom(tb)
        .where(tb.FILE_SIZE.eq(0))
        .or(tb.MEDIA_TYPE.eq(""))
        .or(tb.WIDTH.eq(0))
        .or(tb.HEIGHT.eq(0))

    val count = dsl.fetchCount(query)
    val items =
      query
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(tb)
        .map { it.toDomain() }

    return PageImpl(items, pageable, count.toLong())
  }

  override fun findAllBookIdsByThumbnailTypeAndDimensionSmallerThan(
    type: ThumbnailBook.Type,
    size: Int,
  ): Collection<String> =
    dsl
      .select(tb.BOOK_ID)
      .from(tb)
      .where(tb.TYPE.eq(type.toString()))
      .and(tb.WIDTH.lt(size))
      .and(tb.HEIGHT.lt(size))
      .fetch(tb.BOOK_ID)

  override fun existsById(thumbnailId: String): Boolean = dsl.fetchExists(tb, tb.ID.eq(thumbnailId))

  override fun insert(thumbnail: ThumbnailBook) {
    dsl
      .insertInto(tb)
      .set(tb.ID, thumbnail.id)
      .set(tb.BOOK_ID, thumbnail.bookId)
      .set(tb.THUMBNAIL, thumbnail.thumbnail)
      .set(tb.URL, thumbnail.url?.toString())
      .set(tb.SELECTED, thumbnail.selected)
      .set(tb.TYPE, thumbnail.type.toString())
      .set(tb.MEDIA_TYPE, thumbnail.mediaType)
      .set(tb.WIDTH, thumbnail.dimension.width)
      .set(tb.HEIGHT, thumbnail.dimension.height)
      .set(tb.FILE_SIZE, thumbnail.fileSize)
      .execute()
  }

  override fun update(thumbnail: ThumbnailBook) {
    dsl
      .update(tb)
      .set(tb.BOOK_ID, thumbnail.bookId)
      .set(tb.THUMBNAIL, thumbnail.thumbnail)
      .set(tb.URL, thumbnail.url?.toString())
      .set(tb.SELECTED, thumbnail.selected)
      .set(tb.TYPE, thumbnail.type.toString())
      .set(tb.MEDIA_TYPE, thumbnail.mediaType)
      .set(tb.WIDTH, thumbnail.dimension.width)
      .set(tb.HEIGHT, thumbnail.dimension.height)
      .set(tb.FILE_SIZE, thumbnail.fileSize)
      .where(tb.ID.eq(thumbnail.id))
      .execute()
  }

  override fun updateMetadata(thumbnails: Collection<ThumbnailBook>) {
    dsl.batched { c ->
      thumbnails.forEach {
        c
          .dsl()
          .update(tb)
          .set(tb.MEDIA_TYPE, it.mediaType)
          .set(tb.WIDTH, it.dimension.width)
          .set(tb.HEIGHT, it.dimension.height)
          .set(tb.FILE_SIZE, it.fileSize)
          .where(tb.ID.eq(it.id))
          .execute()
      }
    }
  }

  @Transactional
  override fun markSelected(thumbnail: ThumbnailBook) {
    dsl
      .update(tb)
      .set(tb.SELECTED, false)
      .where(tb.BOOK_ID.eq(thumbnail.bookId))
      .and(tb.ID.ne(thumbnail.id))
      .execute()

    dsl
      .update(tb)
      .set(tb.SELECTED, true)
      .where(tb.BOOK_ID.eq(thumbnail.bookId))
      .and(tb.ID.eq(thumbnail.id))
      .execute()
  }

  override fun delete(thumbnailBookId: String) {
    dsl.deleteFrom(tb).where(tb.ID.eq(thumbnailBookId)).execute()
  }

  override fun deleteByBookId(bookId: String) {
    dsl.deleteFrom(tb).where(tb.BOOK_ID.eq(bookId)).execute()
  }

  @Transactional
  override fun deleteByBookIds(bookIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, bookIds)

    dsl.deleteFrom(tb).where(tb.BOOK_ID.`in`(dsl.selectTempStrings())).execute()
  }

  override fun deleteByBookIdAndType(
    bookId: String,
    type: ThumbnailBook.Type,
  ) {
    dsl
      .deleteFrom(tb)
      .where(tb.BOOK_ID.eq(bookId))
      .and(tb.TYPE.eq(type.toString()))
      .execute()
  }

  private fun ThumbnailBookRecord.toDomain() =
    ThumbnailBook(
      thumbnail = thumbnail,
      url = url?.let { URL(it) },
      selected = selected,
      type = ThumbnailBook.Type.valueOf(type),
      mediaType = mediaType,
      fileSize = fileSize,
      dimension = Dimension(width, height),
      id = id,
      bookId = bookId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate,
    )
}
