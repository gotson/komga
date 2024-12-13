package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.ThumbnailReadList
import org.gotson.komga.domain.persistence.ThumbnailReadListRepository
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.ThumbnailReadlistRecord
import org.jooq.DSLContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ThumbnailReadListDao(
  private val dsl: DSLContext,
) : ThumbnailReadListRepository {
  private val tr = Tables.THUMBNAIL_READLIST

  override fun findAllByReadListId(readListId: String): Collection<ThumbnailReadList> =
    dsl
      .selectFrom(tr)
      .where(tr.READLIST_ID.eq(readListId))
      .fetchInto(tr)
      .map { it.toDomain() }

  override fun findByIdOrNull(thumbnailId: String): ThumbnailReadList? =
    dsl
      .selectFrom(tr)
      .where(tr.ID.eq(thumbnailId))
      .fetchOneInto(tr)
      ?.toDomain()

  override fun findSelectedByReadListIdOrNull(readListId: String): ThumbnailReadList? =
    dsl
      .selectFrom(tr)
      .where(tr.READLIST_ID.eq(readListId))
      .and(tr.SELECTED.isTrue)
      .limit(1)
      .fetchInto(tr)
      .map { it.toDomain() }
      .firstOrNull()

  override fun findAllWithoutMetadata(pageable: Pageable): Page<ThumbnailReadList> {
    val query =
      dsl
        .selectFrom(tr)
        .where(tr.FILE_SIZE.eq(0))
        .or(tr.MEDIA_TYPE.eq(""))
        .or(tr.WIDTH.eq(0))
        .or(tr.HEIGHT.eq(0))

    val count = query.count()
    val items =
      query
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetchInto(tr)
        .map { it.toDomain() }

    return PageImpl(items, pageable, count.toLong())
  }

  override fun insert(thumbnail: ThumbnailReadList) {
    dsl
      .insertInto(tr)
      .set(tr.ID, thumbnail.id)
      .set(tr.READLIST_ID, thumbnail.readListId)
      .set(tr.THUMBNAIL, thumbnail.thumbnail)
      .set(tr.SELECTED, thumbnail.selected)
      .set(tr.TYPE, thumbnail.type.toString())
      .set(tr.MEDIA_TYPE, thumbnail.mediaType)
      .set(tr.WIDTH, thumbnail.dimension.width)
      .set(tr.HEIGHT, thumbnail.dimension.height)
      .set(tr.FILE_SIZE, thumbnail.fileSize)
      .execute()
  }

  override fun update(thumbnail: ThumbnailReadList) {
    dsl
      .update(tr)
      .set(tr.READLIST_ID, thumbnail.readListId)
      .set(tr.THUMBNAIL, thumbnail.thumbnail)
      .set(tr.SELECTED, thumbnail.selected)
      .set(tr.TYPE, thumbnail.type.toString())
      .set(tr.MEDIA_TYPE, thumbnail.mediaType)
      .set(tr.WIDTH, thumbnail.dimension.width)
      .set(tr.HEIGHT, thumbnail.dimension.height)
      .set(tr.FILE_SIZE, thumbnail.fileSize)
      .where(tr.ID.eq(thumbnail.id))
      .execute()
  }

  override fun updateMetadata(thumbnails: Collection<ThumbnailReadList>) {
    dsl.batched { c ->
      thumbnails.forEach {
        c
          .dsl()
          .update(tr)
          .set(tr.MEDIA_TYPE, it.mediaType)
          .set(tr.WIDTH, it.dimension.width)
          .set(tr.HEIGHT, it.dimension.height)
          .set(tr.FILE_SIZE, it.fileSize)
          .where(tr.ID.eq(it.id))
          .execute()
      }
    }
  }

  @Transactional
  override fun markSelected(thumbnail: ThumbnailReadList) {
    dsl
      .update(tr)
      .set(tr.SELECTED, false)
      .where(tr.READLIST_ID.eq(thumbnail.readListId))
      .and(tr.ID.ne(thumbnail.id))
      .execute()

    dsl
      .update(tr)
      .set(tr.SELECTED, true)
      .where(tr.READLIST_ID.eq(thumbnail.readListId))
      .and(tr.ID.eq(thumbnail.id))
      .execute()
  }

  override fun delete(thumbnailReadListId: String) {
    dsl.deleteFrom(tr).where(tr.ID.eq(thumbnailReadListId)).execute()
  }

  override fun deleteByReadListId(readListId: String) {
    dsl.deleteFrom(tr).where(tr.READLIST_ID.eq(readListId)).execute()
  }

  override fun deleteByReadListIds(readListIds: Collection<String>) {
    dsl.deleteFrom(tr).where(tr.READLIST_ID.`in`(readListIds)).execute()
  }

  private fun ThumbnailReadlistRecord.toDomain() =
    ThumbnailReadList(
      thumbnail = thumbnail,
      selected = selected,
      type = ThumbnailReadList.Type.valueOf(type),
      mediaType = mediaType,
      fileSize = fileSize,
      dimension = Dimension(width, height),
      id = id,
      readListId = readlistId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate,
    )
}
