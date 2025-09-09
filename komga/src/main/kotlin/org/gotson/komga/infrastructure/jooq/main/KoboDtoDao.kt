package org.gotson.komga.infrastructure.jooq.main

import com.fasterxml.jackson.databind.ObjectMapper
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.infrastructure.jooq.SplitDslDaoBase
import org.gotson.komga.infrastructure.jooq.deserializeMediaExtension
import org.gotson.komga.interfaces.api.kobo.dto.ContributorDto
import org.gotson.komga.interfaces.api.kobo.dto.KoboBookMetadataDto
import org.gotson.komga.interfaces.api.kobo.dto.KoboSeriesDto
import org.gotson.komga.interfaces.api.kobo.dto.PublisherDto
import org.gotson.komga.interfaces.api.kobo.persistence.KoboDtoRepository
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class KoboDtoDao(
  dslRW: DSLContext,
  @Qualifier("dslContextRO") dslRO: DSLContext,
  private val mapper: ObjectMapper,
) : SplitDslDaoBase(dslRW, dslRO),
  KoboDtoRepository {
  private val b = Tables.BOOK
  private val m = Tables.MEDIA
  private val d = Tables.BOOK_METADATA
  private val a = Tables.BOOK_METADATA_AUTHOR
  private val sd = Tables.SERIES_METADATA
  private val bt = Tables.THUMBNAIL_BOOK

  override fun findBookMetadataByIds(
    bookIds: Collection<String>,
  ): Collection<KoboBookMetadataDto> {
    val records =
      dslRO
        .select(
          d.BOOK_ID,
          d.TITLE,
          d.NUMBER,
          d.NUMBER_SORT,
          d.ISBN,
          d.SUMMARY,
          d.RELEASE_DATE,
          d.CREATED_DATE,
          sd.SERIES_ID,
          sd.TITLE,
          sd.PUBLISHER,
          sd.LANGUAGE,
          b.FILE_SIZE,
          b.ONESHOT,
          m.EPUB_IS_KEPUB,
          m.EXTENSION_CLASS,
          m.EXTENSION_VALUE_BLOB,
          bt.ID,
        ).from(b)
        .leftJoin(d)
        .on(b.ID.eq(d.BOOK_ID))
        .leftJoin(sd)
        .on(b.SERIES_ID.eq(sd.SERIES_ID))
        .leftJoin(m)
        .on(b.ID.eq(m.BOOK_ID))
        .leftJoin(bt)
        .on(b.ID.eq(bt.BOOK_ID))
        .and(bt.SELECTED.isTrue)
        .where(d.BOOK_ID.`in`(bookIds))
        .fetch()

    return records.map { rec ->
      val br = rec.into(b)
      val dr = rec.into(d)
      val sr = rec.into(sd)
      val mr = rec.into(m)
      val btr = rec.into(bt)
      val mediaExtension = mapper.deserializeMediaExtension(mr.extensionClass, mr.extensionValueBlob) as? MediaExtensionEpub

      val authors =
        dslRO
          .selectFrom(a)
          .where(a.BOOK_ID.`in`(bookIds))
          .filter { it.name != null }
          .groupBy({ it.bookId }, { it })

      KoboBookMetadataDto(
        contributorRoles = authors[dr.bookId].orEmpty().map { ContributorDto(it.name) },
        contributors = authors[dr.bookId].orEmpty().map { it.name },
        coverImageId = btr.id,
        crossRevisionId = dr.bookId,
        // if null or empty Kobo will not update it, force it to blank
        description = dr.summary.ifEmpty { " " },
        entitlementId = dr.bookId,
        isbn = dr.isbn.ifBlank { null },
        language = sr.language.take(2).ifBlank { "en" },
        publicationDate = dr.releaseDate?.atStartOfDay(ZoneId.of("Z")) ?: dr.createdDate.atZone(ZoneId.of("Z")),
        publisher = PublisherDto(name = sr.publisher),
        revisionId = dr.bookId,
        series =
          if (!br.oneshot)
            KoboSeriesDto(
              id = sr.seriesId,
              name = sr.title,
              number = dr.number,
              numberFloat = dr.numberSort,
            )
          else
            null,
        title = dr.title,
        workId = dr.bookId,
        isKepub = mr.epubIsKepub,
        isPrePaginated = mediaExtension?.isFixedLayout == true,
        fileSize = br.fileSize,
      )
    }
  }
}
