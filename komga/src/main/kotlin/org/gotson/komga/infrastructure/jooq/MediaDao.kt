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
import java.time.ZoneId

@Component
class MediaDao(
  private val dsl: DSLContext
) : MediaRepository {

  private val m = Tables.MEDIA
  private val p = Tables.MEDIA_PAGE
  private val f = Tables.MEDIA_FILE

  private val groupFields = arrayOf(*m.fields(), *p.fields())

  override fun findById(bookId: String): Media =
    find(dsl, bookId)

  private fun find(dsl: DSLContext, bookId: String): Media =
    dsl.select(*groupFields)
      .from(m)
      .leftJoin(p).on(m.BOOK_ID.eq(p.BOOK_ID))
      .where(m.BOOK_ID.eq(bookId))
      .groupBy(*groupFields)
      .orderBy(p.NUMBER.asc())
      .fetchGroups(
        { it.into(m) }, { it.into(p) }
      ).map { (mr, pr) ->
        val files = dsl.selectFrom(f)
          .where(f.BOOK_ID.eq(bookId))
          .fetchInto(f)
          .map { it.fileName }

        mr.toDomain(pr.filterNot { it.bookId == null }.map { it.toDomain() }, files)
      }.first()

  override fun getThumbnail(bookId: String): ByteArray? =
    dsl.select(m.THUMBNAIL)
      .from(m)
      .where(m.BOOK_ID.eq(bookId))
      .fetchOne(0, ByteArray::class.java)


  override fun insert(media: Media) {
    insertMany(listOf(media))
  }

  override fun insertMany(medias: Collection<Media>) {
    if (medias.isNotEmpty()) {
      dsl.transaction { config ->
        config.dsl().batch(
          config.dsl().insertInto(
            m,
            m.BOOK_ID,
            m.STATUS,
            m.MEDIA_TYPE,
            m.THUMBNAIL,
            m.COMMENT,
            m.PAGE_COUNT
          ).values(null as String?, null, null, null, null, null)
        ).also { step ->
          medias.forEach {
            step.bind(
              it.bookId,
              it.status,
              it.mediaType,
              it.thumbnail,
              it.comment,
              it.pages.size
            )
          }
        }.execute()

        insertPages(config.dsl(), medias)
        insertFiles(config.dsl(), medias)
      }
    }
  }

  private fun insertPages(dsl: DSLContext, medias: Collection<Media>) {
    if (medias.any { it.pages.isNotEmpty() }) {
      dsl.batch(
        dsl.insertInto(
          p,
          p.BOOK_ID,
          p.FILE_NAME,
          p.MEDIA_TYPE,
          p.NUMBER
        ).values(null as String?, null, null, null)
      ).also {
        medias.forEach { media ->
          media.pages.forEachIndexed { index, page ->
            it.bind(
              media.bookId,
              page.fileName,
              page.mediaType,
              index
            )
          }
        }
      }.execute()
    }
  }

  private fun insertFiles(dsl: DSLContext, medias: Collection<Media>) {
    if (medias.any { it.files.isNotEmpty() }) {
      dsl.batch(
        dsl.insertInto(
          f,
          f.BOOK_ID,
          f.FILE_NAME
        ).values(null as String?, null)
      ).also { step ->
        medias.forEach { media ->
          media.files.forEach {
            step.bind(
              media.bookId,
              it
            )
          }
        }
      }.execute()
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
          .set(m.PAGE_COUNT, media.pages.size)
          .set(m.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
          .where(m.BOOK_ID.eq(media.bookId))
          .execute()

        deleteFrom(p)
          .where(p.BOOK_ID.eq(media.bookId))
          .execute()

        deleteFrom(f)
          .where(f.BOOK_ID.eq(media.bookId))
          .execute()

        insertPages(this, listOf(media))
        insertFiles(this, listOf(media))
      }
    }
  }

  override fun delete(bookId: String) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(p).where(p.BOOK_ID.eq(bookId)).execute()
        deleteFrom(f).where(f.BOOK_ID.eq(bookId)).execute()
        deleteFrom(m).where(m.BOOK_ID.eq(bookId)).execute()
      }
    }
  }

  override fun deleteByBookIds(bookIds: Collection<String>) {
    dsl.transaction { config ->
      with(config.dsl())
      {
        deleteFrom(p).where(p.BOOK_ID.`in`(bookIds)).execute()
        deleteFrom(f).where(f.BOOK_ID.`in`(bookIds)).execute()
        deleteFrom(m).where(m.BOOK_ID.`in`(bookIds)).execute()
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
      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone()
    )

  private fun MediaPageRecord.toDomain() =
    BookPage(
      fileName = fileName,
      mediaType = mediaType
    )
}
