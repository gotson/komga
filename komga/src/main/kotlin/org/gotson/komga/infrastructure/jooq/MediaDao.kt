package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.MediaPageRecord
import org.gotson.komga.jooq.tables.records.MediaRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MediaDao(
  private val dsl: DSLContext
) : MediaRepository {

  private val m = Tables.MEDIA
  private val p = Tables.MEDIA_PAGE
  private val f = Tables.MEDIA_FILE

  private val groupFields = arrayOf(*m.fields(), *p.fields())

  override fun findById(bookId: Long): Media =
    dsl.select(*groupFields)
      .from(m)
      .leftJoin(p).on(m.BOOK_ID.eq(p.BOOK_ID))
      .where(m.BOOK_ID.eq(bookId))
      .groupBy(*groupFields)
      .fetchGroups(
        { it.into(m) }, { it.into(p) }
      ).map { (mr, pr) ->
        val files = dsl.selectFrom(f)
          .where(f.BOOK_ID.eq(bookId))
          .fetchInto(f)
          .map { it.fileName }

        mr.toDomain(pr.filterNot { it.bookId == null }.map { it.toDomain() }, files)
      }.first()


  override fun getThumbnail(bookId: Long): ByteArray? =
    dsl.select(m.THUMBNAIL)
      .from(m)
      .where(m.BOOK_ID.eq(bookId))
      .fetchOne(0, ByteArray::class.java)


  override fun insert(media: Media): Media {
    dsl.transaction { config ->
      with(config.dsl())
      {
        insertInto(m)
          .set(m.BOOK_ID, media.bookId)
          .set(m.STATUS, media.status.toString())
          .set(m.MEDIA_TYPE, media.mediaType)
          .set(m.THUMBNAIL, media.thumbnail)
          .set(m.COMMENT, media.comment)
          .set(m.PAGE_COUNT, media.pages.size.toLong())
          .execute()

        insertPages(this, media)
        insertFiles(this, media)
      }
    }

    return findById(media.bookId)
  }

  private fun insertPages(dsl: DSLContext, media: Media) {
    media.pages.forEach {
      dsl.insertInto(p)
        .set(p.BOOK_ID, media.bookId)
        .set(p.FILE_NAME, it.fileName)
        .set(p.MEDIA_TYPE, it.mediaType)
        .execute()
    }
  }

  private fun insertFiles(dsl: DSLContext, media: Media) {
    media.files.forEach {
      dsl.insertInto(f)
        .set(f.BOOK_ID, media.bookId)
        .set(f.FILE_NAME, it)
        .execute()
    }
  }

  override fun update(media: Media) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        update(m)
          .set(m.STATUS, media.status.toString())
          .set(m.MEDIA_TYPE, media.mediaType)
          .set(m.THUMBNAIL, media.thumbnail)
          .set(m.COMMENT, media.comment)
          .set(m.PAGE_COUNT, media.pages.size.toLong())
          .set(m.LAST_MODIFIED_DATE, LocalDateTime.now())
          .where(m.BOOK_ID.eq(media.bookId))
          .execute()

        deleteFrom(p)
          .where(p.BOOK_ID.eq(media.bookId))
          .execute()

        deleteFrom(f)
          .where(f.BOOK_ID.eq(media.bookId))
          .execute()

        insertPages(this, media)
        insertFiles(this, media)
      }
    }
  }

  override fun delete(bookId: Long) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(p).where(p.BOOK_ID.eq(bookId)).execute()
        deleteFrom(f).where(f.BOOK_ID.eq(bookId)).execute()
        deleteFrom(m).where(m.BOOK_ID.eq(bookId)).execute()
      }
    }
  }

  private fun MediaRecord.toDomain(pages: List<BookPage>, files: List<String>) =
    Media(
      status = Media.Status.valueOf(status),
      mediaType = mediaType,
      thumbnail = thumbnail,
      pages = pages,
      files = files,
      comment = comment,
      bookId = bookId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )

  private fun MediaPageRecord.toDomain() =
    BookPage(
      fileName = fileName,
      mediaType = mediaType
    )
}
