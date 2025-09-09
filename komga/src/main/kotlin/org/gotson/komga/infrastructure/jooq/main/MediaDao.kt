package org.gotson.komga.infrastructure.jooq.main

import com.fasterxml.jackson.databind.ObjectMapper
import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaExtension
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.ProxyExtension
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.infrastructure.jooq.SplitDslDaoBase
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.gotson.komga.infrastructure.jooq.deserializeMediaExtension
import org.gotson.komga.infrastructure.jooq.serializeJsonGz
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.MediaFileRecord
import org.gotson.komga.jooq.main.tables.records.MediaPageRecord
import org.gotson.komga.jooq.main.tables.records.MediaRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class MediaDao(
  dslRW: DSLContext,
  @Qualifier("dslContextRO") dslRO: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
  private val mapper: ObjectMapper,
) : SplitDslDaoBase(dslRW, dslRO),
  MediaRepository {
  private val m = Tables.MEDIA
  private val p = Tables.MEDIA_PAGE
  private val f = Tables.MEDIA_FILE
  private val b = Tables.BOOK

  private val groupFields =
    arrayOf(
      m.BOOK_ID,
      m.MEDIA_TYPE,
      m.STATUS,
      m.CREATED_DATE,
      m.LAST_MODIFIED_DATE,
      m.COMMENT,
      m.PAGE_COUNT,
      m.EXTENSION_CLASS,
      m.EPUB_DIVINA_COMPATIBLE,
      m.EPUB_IS_KEPUB,
      *p.fields(),
    )

  override fun findById(bookId: String): Media = dslRO.find(bookId)!!

  override fun findByIdOrNull(bookId: String): Media? = dslRO.find(bookId)

  override fun findExtensionByIdOrNull(bookId: String): MediaExtension? =
    dslRO
      .select(m.EXTENSION_CLASS, m.EXTENSION_VALUE_BLOB)
      .from(m)
      .where(m.BOOK_ID.eq(bookId))
      .fetchOne()
      ?.map { mapper.deserializeMediaExtension(it.get(m.EXTENSION_CLASS), it.get(m.EXTENSION_VALUE_BLOB)) }

  override fun findAllBookIdsByLibraryIdAndMediaTypeAndWithMissingPageHash(
    libraryId: String,
    mediaTypes: Collection<String>,
    pageHashing: Int,
  ): Collection<String> {
    val pagesCount = DSL.count(p.BOOK_ID)
    val hashedCount = DSL.sum(DSL.`when`(p.FILE_HASH.eq(""), 0).otherwise(1)).cast(Int::class.java)
    val neededHash = pageHashing * 2
    val neededHashForBook = DSL.`when`(pagesCount.lt(neededHash), pagesCount).otherwise(neededHash)

    return dslRO
      .select(b.ID)
      .from(b)
      .leftJoin(p)
      .on(b.ID.eq(p.BOOK_ID))
      .leftJoin(m)
      .on(b.ID.eq(m.BOOK_ID))
      .where(b.LIBRARY_ID.eq(libraryId))
      .and(m.STATUS.eq(Media.Status.READY.name))
      .and(m.MEDIA_TYPE.`in`(mediaTypes))
      .groupBy(b.ID)
      .having(hashedCount.lt(neededHashForBook))
      .fetch()
      .map { it.value1() }
  }

  override fun getPagesSizes(bookIds: Collection<String>): Collection<Pair<String, Int>> =
    dslRO
      .select(m.BOOK_ID, m.PAGE_COUNT)
      .from(m)
      .where(m.BOOK_ID.`in`(bookIds))
      .fetch()
      .map { Pair(it[m.BOOK_ID], it[m.PAGE_COUNT]) }

  private fun DSLContext.find(
    bookId: String,
  ): Media? =
    this
      .select(*groupFields)
      .from(m)
      .leftJoin(p)
      .on(m.BOOK_ID.eq(p.BOOK_ID))
      .where(m.BOOK_ID.eq(bookId))
      .groupBy(*groupFields)
      .orderBy(p.NUMBER.asc())
      .fetchGroups(
        { it.into(m) },
        { it.into(p) },
      ).map { (mr, pr) ->
        val files =
          this
            .selectFrom(f)
            .where(f.BOOK_ID.eq(bookId))
            .fetchInto(f)

        mr.toDomain(pr.filterNot { it.bookId == null }.map { it.toDomain() }, files.map { it.toDomain() })
      }.firstOrNull()

  @Transactional
  override fun insert(media: Media) {
    insert(listOf(media))
  }

  @Transactional
  override fun insert(medias: Collection<Media>) {
    if (medias.isNotEmpty()) {
      medias.chunked(batchSize).forEach { chunk ->
        dslRW
          .batch(
            dslRW
              .insertInto(
                m,
                m.BOOK_ID,
                m.STATUS,
                m.MEDIA_TYPE,
                m.COMMENT,
                m.PAGE_COUNT,
                m.EPUB_DIVINA_COMPATIBLE,
                m.EPUB_IS_KEPUB,
                m.EXTENSION_CLASS,
                m.EXTENSION_VALUE_BLOB,
              ).values(null as String?, null, null, null, null, null, null, null, null),
          ).also { step ->
            chunk.forEach { media ->
              step.bind(
                media.bookId,
                media.status,
                media.mediaType,
                media.comment,
                media.pageCount,
                media.epubDivinaCompatible,
                media.epubIsKepub,
                media.extension?.let { if (it is ProxyExtension) null else it::class.qualifiedName },
                media.extension?.let { if (it is ProxyExtension) null else mapper.serializeJsonGz(it) },
              )
            }
          }.execute()
      }

      dslRW.insertPages(medias)
      dslRW.insertFiles(medias)
    }
  }

  private fun DSLContext.insertPages(medias: Collection<Media>) {
    if (medias.any { it.pages.isNotEmpty() }) {
      medias.chunked(batchSize).forEach { chunk ->
        this
          .batch(
            this
              .insertInto(
                p,
                p.BOOK_ID,
                p.FILE_NAME,
                p.MEDIA_TYPE,
                p.NUMBER,
                p.WIDTH,
                p.HEIGHT,
                p.FILE_HASH,
                p.FILE_SIZE,
              ).values(null as String?, null, null, null, null, null, null, null),
          ).also { step ->
            chunk.forEach { media ->
              media.pages.forEachIndexed { index, page ->
                step.bind(
                  media.bookId,
                  page.fileName,
                  page.mediaType,
                  index,
                  page.dimension?.width,
                  page.dimension?.height,
                  page.fileHash,
                  page.fileSize,
                )
              }
            }
          }.execute()
      }
    }
  }

  private fun DSLContext.insertFiles(medias: Collection<Media>) {
    if (medias.any { it.files.isNotEmpty() }) {
      medias.chunked(batchSize).forEach { chunk ->
        this
          .batch(
            this
              .insertInto(
                f,
                f.BOOK_ID,
                f.FILE_NAME,
                f.MEDIA_TYPE,
                f.SUB_TYPE,
                f.FILE_SIZE,
              ).values(null as String?, null, null, null, null),
          ).also { step ->
            chunk.forEach { media ->
              media.files.forEach {
                step.bind(
                  media.bookId,
                  it.fileName,
                  it.mediaType,
                  it.subType,
                  it.fileSize,
                )
              }
            }
          }.execute()
      }
    }
  }

  @Transactional
  override fun update(media: Media) {
    dslRW
      .update(m)
      .set(m.STATUS, media.status.toString())
      .set(m.MEDIA_TYPE, media.mediaType)
      .set(m.COMMENT, media.comment)
      .set(m.PAGE_COUNT, media.pageCount)
      .set(m.EPUB_DIVINA_COMPATIBLE, media.epubDivinaCompatible)
      .set(m.EPUB_IS_KEPUB, media.epubIsKepub)
      .apply {
        if (media.extension != null && media.extension !is ProxyExtension) {
          set(m.EXTENSION_CLASS, media.extension::class.qualifiedName)
          set(m.EXTENSION_VALUE_BLOB, mapper.serializeJsonGz(media.extension))
        }
      }.set(m.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(m.BOOK_ID.eq(media.bookId))
      .execute()

    dslRW
      .deleteFrom(p)
      .where(p.BOOK_ID.eq(media.bookId))
      .execute()

    dslRW
      .deleteFrom(f)
      .where(f.BOOK_ID.eq(media.bookId))
      .execute()

    dslRW.insertPages(listOf(media))
    dslRW.insertFiles(listOf(media))
  }

  @Transactional
  override fun delete(bookId: String) {
    dslRW.deleteFrom(p).where(p.BOOK_ID.eq(bookId)).execute()
    dslRW.deleteFrom(f).where(f.BOOK_ID.eq(bookId)).execute()
    dslRW.deleteFrom(m).where(m.BOOK_ID.eq(bookId)).execute()
  }

  @Transactional
  override fun delete(bookIds: Collection<String>) {
    dslRW.withTempTable(batchSize, bookIds).use {
      dslRW.deleteFrom(p).where(p.BOOK_ID.`in`(it.selectTempStrings())).execute()
      dslRW.deleteFrom(f).where(f.BOOK_ID.`in`(it.selectTempStrings())).execute()
      dslRW.deleteFrom(m).where(m.BOOK_ID.`in`(it.selectTempStrings())).execute()
    }
  }

  override fun count(): Long = dslRO.fetchCount(m).toLong()

  private fun MediaRecord.toDomain(
    pages: List<BookPage>,
    files: List<MediaFile>,
  ) = Media(
    status = Media.Status.valueOf(status),
    mediaType = mediaType,
    pages = pages,
    pageCount = pageCount,
    files = files,
    extension = ProxyExtension.of(extensionClass),
    comment = comment,
    bookId = bookId,
    epubDivinaCompatible = epubDivinaCompatible,
    epubIsKepub = epubIsKepub,
    createdDate = createdDate.toCurrentTimeZone(),
    lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
  )

  private fun MediaPageRecord.toDomain() =
    BookPage(
      fileName = fileName,
      mediaType = mediaType,
      dimension = if (width != null && height != null) Dimension(width, height) else null,
      fileHash = fileHash,
      fileSize = fileSize,
    )

  private fun MediaFileRecord.toDomain() =
    MediaFile(
      fileName = fileName,
      mediaType = mediaType,
      subType = subType?.let { MediaFile.SubType.valueOf(it) },
      fileSize = fileSize,
    )
}
