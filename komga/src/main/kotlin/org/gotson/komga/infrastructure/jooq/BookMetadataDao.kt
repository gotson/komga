package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.BookMetadataAuthorRecord
import org.gotson.komga.jooq.tables.records.BookMetadataRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class BookMetadataDao(
  private val dsl: DSLContext
) : BookMetadataRepository {

  private val d = Tables.BOOK_METADATA
  private val a = Tables.BOOK_METADATA_AUTHOR

  private val groupFields = arrayOf(*d.fields(), *a.fields())

  override fun findById(bookId: Long): BookMetadata =
    find(dsl, listOf(bookId)).first()

  override fun findByIdOrNull(bookId: Long): BookMetadata? =
    find(dsl, listOf(bookId)).firstOrNull()

  override fun findByIds(bookIds: Collection<Long>): Collection<BookMetadata> =
    find(dsl, bookIds)

  private fun find(dsl: DSLContext, bookIds: Collection<Long>) =
    dsl.select(*groupFields)
      .from(d)
      .leftJoin(a).on(d.BOOK_ID.eq(a.BOOK_ID))
      .where(d.BOOK_ID.`in`(bookIds))
      .groupBy(*groupFields)
      .fetchGroups(
        { it.into(d) }, { it.into(a) }
      ).map { (dr, ar) ->
        dr.toDomain(ar.filterNot { it.name == null }.map { it.toDomain() })
      }

  override fun findAuthorsByName(search: String): List<String> {
    return dsl.selectDistinct(a.NAME)
      .from(a)
      .where(a.NAME.containsIgnoreCase(search))
      .orderBy(a.NAME)
      .fetch(a.NAME)
  }

  override fun insert(metadata: BookMetadata): BookMetadata =
    dsl.transactionResult { config ->
      insertMetadata(config.dsl(), metadata)
    }

  override fun insertMany(metadatas: Collection<BookMetadata>): Collection<BookMetadata> =
    dsl.transactionResult { config ->
      metadatas.map { insertMetadata(config.dsl(), it) }
    }

  private fun insertMetadata(dsl: DSLContext, metadata: BookMetadata): BookMetadata {
    dsl.insertInto(d)
      .set(d.BOOK_ID, metadata.bookId)
      .set(d.TITLE, metadata.title)
      .set(d.TITLE_LOCK, metadata.titleLock)
      .set(d.SUMMARY, metadata.summary)
      .set(d.SUMMARY_LOCK, metadata.summaryLock)
      .set(d.NUMBER, metadata.number)
      .set(d.NUMBER_LOCK, metadata.numberLock)
      .set(d.NUMBER_SORT, metadata.numberSort)
      .set(d.NUMBER_SORT_LOCK, metadata.numberSortLock)
      .set(d.READING_DIRECTION, metadata.readingDirection?.toString())
      .set(d.READING_DIRECTION_LOCK, metadata.readingDirectionLock)
      .set(d.PUBLISHER, metadata.publisher)
      .set(d.PUBLISHER_LOCK, metadata.publisherLock)
      .set(d.AGE_RATING, metadata.ageRating)
      .set(d.AGE_RATING_LOCK, metadata.ageRatingLock)
      .set(d.RELEASE_DATE, metadata.releaseDate)
      .set(d.RELEASE_DATE_LOCK, metadata.releaseDateLock)
      .set(d.AUTHORS_LOCK, metadata.authorsLock)
      .execute()

    insertAuthors(dsl, metadata)

    return find(dsl, listOf(metadata.bookId)).first()
  }

  override fun update(metadata: BookMetadata) {
    dsl.transaction { config ->
      updateMetadata(config.dsl(), metadata)
    }
  }

  override fun updateMany(metadatas: Collection<BookMetadata>) {
    dsl.transaction { config ->
      metadatas.forEach { updateMetadata(config.dsl(), it) }
    }
  }

  private fun updateMetadata(dsl: DSLContext, metadata: BookMetadata) {
    dsl.update(d)
      .set(d.TITLE, metadata.title)
      .set(d.TITLE_LOCK, metadata.titleLock)
      .set(d.SUMMARY, metadata.summary)
      .set(d.SUMMARY_LOCK, metadata.summaryLock)
      .set(d.NUMBER, metadata.number)
      .set(d.NUMBER_LOCK, metadata.numberLock)
      .set(d.NUMBER_SORT, metadata.numberSort)
      .set(d.NUMBER_SORT_LOCK, metadata.numberSortLock)
      .set(d.READING_DIRECTION, metadata.readingDirection?.toString())
      .set(d.READING_DIRECTION_LOCK, metadata.readingDirectionLock)
      .set(d.PUBLISHER, metadata.publisher)
      .set(d.PUBLISHER_LOCK, metadata.publisherLock)
      .set(d.AGE_RATING, metadata.ageRating)
      .set(d.AGE_RATING_LOCK, metadata.ageRatingLock)
      .set(d.RELEASE_DATE, metadata.releaseDate)
      .set(d.RELEASE_DATE_LOCK, metadata.releaseDateLock)
      .set(d.AUTHORS_LOCK, metadata.authorsLock)
      .set(d.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(d.BOOK_ID.eq(metadata.bookId))
      .execute()

    dsl.deleteFrom(a)
      .where(a.BOOK_ID.eq(metadata.bookId))
      .execute()

    insertAuthors(dsl, metadata)
  }

  private fun insertAuthors(dsl: DSLContext, metadata: BookMetadata) {
    metadata.authors.forEach {
      dsl.insertInto(a)
        .set(a.BOOK_ID, metadata.bookId)
        .set(a.NAME, it.name)
        .set(a.ROLE, it.role)
        .execute()
    }
  }

  override fun delete(bookId: Long) {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(a).where(a.BOOK_ID.eq(bookId)).execute()
        deleteFrom(d).where(d.BOOK_ID.eq(bookId)).execute()
      }
    }
  }

  override fun deleteByBookIds(bookIds: Collection<Long>) {
    dsl.transaction { config ->
      with(config.dsl()) {
        deleteFrom(a).where(a.BOOK_ID.`in`(bookIds)).execute()
        deleteFrom(d).where(d.BOOK_ID.`in`(bookIds)).execute()
      }
    }
  }

  private fun BookMetadataRecord.toDomain(authors: Collection<Author>) =
    BookMetadata(
      title = title,
      summary = summary,
      number = number,
      numberSort = numberSort,
      readingDirection = readingDirection?.let {
        BookMetadata.ReadingDirection.valueOf(readingDirection)
      },
      publisher = publisher,
      ageRating = ageRating,
      releaseDate = releaseDate,
      authors = authors.toMutableList(),

      bookId = bookId,

      createdDate = createdDate.toCurrentTimeZone(),
      lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),

      titleLock = titleLock,
      summaryLock = summaryLock,
      numberLock = numberLock,
      numberSortLock = numberSortLock,
      readingDirectionLock = readingDirectionLock,
      publisherLock = publisherLock,
      ageRatingLock = ageRatingLock,
      releaseDateLock = releaseDateLock,
      authorsLock = authorsLock
    )

  private fun BookMetadataAuthorRecord.toDomain() =
    Author(
      name = name,
      role = role
    )
}
