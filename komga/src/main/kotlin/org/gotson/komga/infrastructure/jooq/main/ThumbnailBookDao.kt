package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.infrastructure.jooq.SplitDslDaoBase
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.ThumbnailBookRecord
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Component
class ThumbnailBookDao(
  dslRW: DSLContext,
  @Qualifier("dslContextRO") dslRO: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : SplitDslDaoBase(dslRW, dslRO),
  ThumbnailBookRepository {
  private val tb = Tables.THUMBNAIL_BOOK

  override fun findAllByBookId(bookId: String): Collection<ThumbnailBook> =
    dslRO
      .selectFrom(tb)
      .where(tb.BOOK_ID.eq(bookId))
      .fetchInto(tb)
      .map { it.toDomain() }

  override fun findAllByBookIdAndType(
    bookId: String,
    type: Set<ThumbnailBook.Type>,
  ): Collection<ThumbnailBook> =
    dslRO
      .selectFrom(tb)
      .where(tb.BOOK_ID.eq(bookId))
      .and(tb.TYPE.`in`(type.map { it.name }))
      .fetchInto(tb)
      .map { it.toDomain() }

  override fun findByIdOrNull(thumbnailId: String): ThumbnailBook? =
    dslRO
      .selectFrom(tb)
      .where(tb.ID.eq(thumbnailId))
      .fetchOneInto(tb)
      ?.toDomain()

  override fun findSelectedByBookIdOrNull(bookId: String): ThumbnailBook? =
    dslRO
      .selectFrom(tb)
      .where(tb.BOOK_ID.eq(bookId))
      .and(tb.SELECTED.isTrue)
      .limit(1)
      .fetchInto(tb)
      .map { it.toDomain() }
      .firstOrNull()

  override fun findAllBookIdsByThumbnailTypeAndDimensionSmallerThan(
    type: ThumbnailBook.Type,
    size: Int,
  ): Collection<String> =
    dslRO
      .select(tb.BOOK_ID)
      .from(tb)
      .where(tb.TYPE.eq(type.toString()))
      .and(tb.WIDTH.lt(size))
      .and(tb.HEIGHT.lt(size))
      .fetch(tb.BOOK_ID)

  override fun existsById(thumbnailId: String): Boolean = dslRO.fetchExists(tb, tb.ID.eq(thumbnailId))

  override fun insert(thumbnail: ThumbnailBook) {
    dslRW
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
    dslRW
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

  @Transactional
  override fun markSelected(thumbnail: ThumbnailBook) {
    dslRW
      .update(tb)
      .set(tb.SELECTED, false)
      .where(tb.BOOK_ID.eq(thumbnail.bookId))
      .and(tb.ID.ne(thumbnail.id))
      .execute()

    dslRW
      .update(tb)
      .set(tb.SELECTED, true)
      .where(tb.BOOK_ID.eq(thumbnail.bookId))
      .and(tb.ID.eq(thumbnail.id))
      .execute()
  }

  override fun delete(thumbnailBookId: String) {
    dslRW.deleteFrom(tb).where(tb.ID.eq(thumbnailBookId)).execute()
  }

  override fun deleteByBookId(bookId: String) {
    dslRW.deleteFrom(tb).where(tb.BOOK_ID.eq(bookId)).execute()
  }

  @Transactional
  override fun deleteByBookIds(bookIds: Collection<String>) {
    dslRW.withTempTable(batchSize, bookIds).use {
      dslRW.deleteFrom(tb).where(tb.BOOK_ID.`in`(it.selectTempStrings())).execute()
    }
  }

  override fun deleteByBookIdAndType(
    bookId: String,
    type: ThumbnailBook.Type,
  ) {
    dslRW
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
