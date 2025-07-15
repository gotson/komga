package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.WebLink
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.infrastructure.jooq.insertTempStrings
import org.gotson.komga.infrastructure.jooq.selectTempStrings
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.BookMetadataAuthorRecord
import org.gotson.komga.jooq.main.tables.records.BookMetadataRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class BookMetadataDao(
  private val dsl: DSLContext,
  @param:Value("#{@komgaProperties.database.batchChunkSize}") private val batchSize: Int,
) : BookMetadataRepository {
  private val d = Tables.BOOK_METADATA
  private val a = Tables.BOOK_METADATA_AUTHOR
  private val bt = Tables.BOOK_METADATA_TAG
  private val bl = Tables.BOOK_METADATA_LINK

  private val groupFields = arrayOf(*d.fields(), *a.fields())

  override fun findById(bookId: String): BookMetadata = find(dsl, listOf(bookId)).first()

  override fun findByIdOrNull(bookId: String): BookMetadata? = find(dsl, listOf(bookId)).firstOrNull()

  override fun findAllByIds(bookIds: Collection<String>): Collection<BookMetadata> = find(dsl, bookIds)

  private fun find(
    dsl: DSLContext,
    bookIds: Collection<String>,
  ) = dsl
    .select(*groupFields)
    .from(d)
    .leftJoin(a)
    .on(d.BOOK_ID.eq(a.BOOK_ID))
    .where(d.BOOK_ID.`in`(bookIds))
    .groupBy(*groupFields)
    .fetchGroups(
      { it.into(d) },
      { it.into(a) },
    ).map { (dr, ar) ->
      dr.toDomain(ar.filterNot { it.name == null }.map { it.toDomain() }, findTags(dr.bookId), findLinks(dr.bookId))
    }

  private fun findTags(bookId: String) =
    dsl
      .select(bt.TAG)
      .from(bt)
      .where(bt.BOOK_ID.eq(bookId))
      .fetchSet(bt.TAG)

  private fun findLinks(bookId: String) =
    dsl
      .select(bl.LABEL, bl.URL)
      .from(bl)
      .where(bl.BOOK_ID.eq(bookId))
      .fetchInto(bl)
      .map { WebLink(it.label, URI(it.url)) }

  @Transactional
  override fun insert(metadata: BookMetadata) {
    insert(listOf(metadata))
  }

  @Transactional
  override fun insert(metadatas: Collection<BookMetadata>) {
    if (metadatas.isNotEmpty()) {
      metadatas.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(
                d,
                d.BOOK_ID,
                d.TITLE,
                d.TITLE_LOCK,
                d.SUMMARY,
                d.SUMMARY_LOCK,
                d.NUMBER,
                d.NUMBER_LOCK,
                d.NUMBER_SORT,
                d.NUMBER_SORT_LOCK,
                d.RELEASE_DATE,
                d.RELEASE_DATE_LOCK,
                d.AUTHORS_LOCK,
                d.TAGS_LOCK,
                d.ISBN,
                d.ISBN_LOCK,
                d.LINKS_LOCK,
              ).values(null as String?, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
          ).also { step ->
            chunk.forEach {
              step.bind(
                it.bookId,
                it.title,
                it.titleLock,
                it.summary,
                it.summaryLock,
                it.number,
                it.numberLock,
                it.numberSort,
                it.numberSortLock,
                it.releaseDate,
                it.releaseDateLock,
                it.authorsLock,
                it.tagsLock,
                it.isbn,
                it.isbnLock,
                it.linksLock,
              )
            }
          }.execute()
      }

      insertAuthors(metadatas)
      insertTags(metadatas)
      insertLinks(metadatas)
    }
  }

  @Transactional
  override fun update(metadata: BookMetadata) {
    updateMetadata(metadata)
  }

  @Transactional
  override fun update(metadatas: Collection<BookMetadata>) {
    metadatas.forEach { updateMetadata(it) }
  }

  private fun updateMetadata(metadata: BookMetadata) {
    dsl
      .update(d)
      .set(d.TITLE, metadata.title)
      .set(d.TITLE_LOCK, metadata.titleLock)
      .set(d.SUMMARY, metadata.summary)
      .set(d.SUMMARY_LOCK, metadata.summaryLock)
      .set(d.NUMBER, metadata.number)
      .set(d.NUMBER_LOCK, metadata.numberLock)
      .set(d.NUMBER_SORT, metadata.numberSort)
      .set(d.NUMBER_SORT_LOCK, metadata.numberSortLock)
      .set(d.RELEASE_DATE, metadata.releaseDate)
      .set(d.RELEASE_DATE_LOCK, metadata.releaseDateLock)
      .set(d.AUTHORS_LOCK, metadata.authorsLock)
      .set(d.TAGS_LOCK, metadata.tagsLock)
      .set(d.ISBN, metadata.isbn)
      .set(d.ISBN_LOCK, metadata.isbnLock)
      .set(d.LINKS_LOCK, metadata.linksLock)
      .set(d.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(d.BOOK_ID.eq(metadata.bookId))
      .execute()

    dsl
      .deleteFrom(a)
      .where(a.BOOK_ID.eq(metadata.bookId))
      .execute()
    dsl
      .deleteFrom(bt)
      .where(bt.BOOK_ID.eq(metadata.bookId))
      .execute()
    dsl
      .deleteFrom(bl)
      .where(bl.BOOK_ID.eq(metadata.bookId))
      .execute()

    insertAuthors(listOf(metadata))
    insertTags(listOf(metadata))
    insertLinks(listOf(metadata))
  }

  private fun insertAuthors(metadatas: Collection<BookMetadata>) {
    if (metadatas.any { it.authors.isNotEmpty() }) {
      metadatas.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(a, a.BOOK_ID, a.NAME, a.ROLE)
              .values(null as String?, null, null),
          ).also { step ->
            chunk.forEach { metadata ->
              metadata.authors.forEach {
                step.bind(metadata.bookId, it.name, it.role)
              }
            }
          }.execute()
      }
    }
  }

  private fun insertTags(metadatas: Collection<BookMetadata>) {
    if (metadatas.any { it.tags.isNotEmpty() }) {
      metadatas.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(bt, bt.BOOK_ID, bt.TAG)
              .values(null as String?, null),
          ).also { step ->
            chunk.forEach { metadata ->
              metadata.tags.forEach {
                step.bind(metadata.bookId, it)
              }
            }
          }.execute()
      }
    }
  }

  private fun insertLinks(metadatas: Collection<BookMetadata>) {
    if (metadatas.any { it.links.isNotEmpty() }) {
      metadatas.chunked(batchSize).forEach { chunk ->
        dsl
          .batch(
            dsl
              .insertInto(bl, bl.BOOK_ID, bl.LABEL, bl.URL)
              .values(null as String?, null, null),
          ).also { step ->
            chunk.forEach { metadata ->
              metadata.links.forEach {
                step.bind(metadata.bookId, it.label, it.url.toString())
              }
            }
          }.execute()
      }
    }
  }

  @Transactional
  override fun delete(bookId: String) {
    dsl.deleteFrom(a).where(a.BOOK_ID.eq(bookId)).execute()
    dsl.deleteFrom(bt).where(bt.BOOK_ID.eq(bookId)).execute()
    dsl.deleteFrom(bl).where(bl.BOOK_ID.eq(bookId)).execute()
    dsl.deleteFrom(d).where(d.BOOK_ID.eq(bookId)).execute()
  }

  @Transactional
  override fun delete(bookIds: Collection<String>) {
    dsl.insertTempStrings(batchSize, bookIds)

    dsl.deleteFrom(a).where(a.BOOK_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(bt).where(bt.BOOK_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(bl).where(bl.BOOK_ID.`in`(dsl.selectTempStrings())).execute()
    dsl.deleteFrom(d).where(d.BOOK_ID.`in`(dsl.selectTempStrings())).execute()
  }

  override fun count(): Long = dsl.fetchCount(d).toLong()

  private fun BookMetadataRecord.toDomain(
    authors: List<Author>,
    tags: Set<String>,
    links: List<WebLink>,
  ) = BookMetadata(
    title = title,
    summary = summary,
    number = number,
    numberSort = numberSort,
    releaseDate = releaseDate,
    authors = authors,
    tags = tags,
    isbn = isbn,
    links = links,
    bookId = bookId,
    createdDate = createdDate.toCurrentTimeZone(),
    lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
    titleLock = titleLock,
    summaryLock = summaryLock,
    numberLock = numberLock,
    numberSortLock = numberSortLock,
    releaseDateLock = releaseDateLock,
    authorsLock = authorsLock,
    tagsLock = tagsLock,
    isbnLock = isbnLock,
    linksLock = linksLock,
  )

  private fun BookMetadataAuthorRecord.toDomain() =
    Author(
      name = name,
      role = role,
    )
}
