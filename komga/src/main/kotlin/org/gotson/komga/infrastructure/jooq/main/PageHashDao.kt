package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.model.PageHashMatch
import org.gotson.komga.domain.model.PageHashUnknown
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.infrastructure.jooq.toOrderBy
import org.gotson.komga.jooq.main.Tables
import org.gotson.komga.jooq.main.tables.records.PageHashRecord
import org.gotson.komga.language.toCurrentTimeZone
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class PageHashDao(
  private val dsl: DSLContext,
) : PageHashRepository {
  private val p = Tables.MEDIA_PAGE
  private val b = Tables.BOOK
  private val ph = Tables.PAGE_HASH
  private val pht = Tables.PAGE_HASH_THUMBNAIL

  private val sortsKnown =
    mapOf(
      "hash" to ph.HASH,
      "matchCount" to DSL.field("count"),
      "deleteCount" to ph.DELETE_COUNT,
      "deleteSize" to ph.SIZE * ph.DELETE_COUNT,
      "fileSize" to ph.SIZE,
      "createdDate" to ph.CREATED_DATE,
      "lastModifiedDate" to ph.LAST_MODIFIED_DATE,
    )

  private val sortsUnknown =
    mapOf(
      "hash" to p.FILE_HASH,
      "fileSize" to p.FILE_SIZE,
      "matchCount" to DSL.field("count"),
      "totalSize" to DSL.field("totalSize"),
      "url" to b.URL,
      "bookId" to b.ID,
      "pageNumber" to p.NUMBER,
    )

  override fun findKnown(pageHash: String): PageHashKnown? =
    dsl
      .selectFrom(ph)
      .where(ph.HASH.eq(pageHash))
      .fetchOneInto(ph)
      ?.toDomain()

  override fun findAllKnown(
    actions: List<PageHashKnown.Action>?,
    pageable: Pageable,
  ): Page<PageHashKnown> {
    val query =
      dsl
        .select(*ph.fields(), DSL.count(p.FILE_HASH).`as`("count"))
        .from(ph)
        .leftJoin(p)
        .on(ph.HASH.eq(p.FILE_HASH))
        .apply { actions?.let { where(ph.ACTION.`in`(actions)) } }
        .groupBy(*ph.fields())

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sortsKnown)
    val items =
      query
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetch()
        .map { it.into(ph).toDomain(it.get("count", Int::class.java)) }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findAllUnknown(pageable: Pageable): Page<PageHashUnknown> {
    val bookCount = DSL.count(p.BOOK_ID)
    val query =
      dsl
        .select(
          p.FILE_HASH,
          p.FILE_SIZE,
          bookCount.`as`("count"),
          (bookCount * p.FILE_SIZE).`as`("totalSize"),
        ).from(p)
        .where(p.FILE_HASH.ne(""))
        .and(
          DSL.notExists(
            dsl
              .selectOne()
              .from(ph)
              .where(ph.HASH.eq(p.FILE_HASH)),
          ),
        ).groupBy(p.FILE_HASH)
        .having(DSL.count(p.BOOK_ID).gt(1))

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sortsUnknown)
    val items =
      query
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetch {
          PageHashUnknown(it.value1(), it.value2(), it.value3())
        }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findMatchesByHash(
    pageHash: String,
    pageable: Pageable,
  ): Page<PageHashMatch> {
    val query =
      dsl
        .select(p.BOOK_ID, b.URL, p.NUMBER, p.FILE_NAME, p.FILE_SIZE, p.MEDIA_TYPE)
        .from(p)
        .leftJoin(b)
        .on(p.BOOK_ID.eq(b.ID))
        .where(p.FILE_HASH.eq(pageHash))

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sortsUnknown)
    val items =
      query
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .fetch {
          PageHashMatch(
            bookId = it.value1(),
            url = URL(it.value2()),
            pageNumber = it.value3() + 1,
            fileName = it.value4(),
            fileSize = it.value5(),
            mediaType = it.value6(),
          )
        }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged)
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else
        PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findMatchesByKnownHashAction(
    actions: List<PageHashKnown.Action>?,
    libraryId: String?,
  ): Map<String, Collection<BookPageNumbered>> =
    dsl
      .select(p.BOOK_ID, p.FILE_NAME, p.NUMBER, p.FILE_HASH, p.MEDIA_TYPE, p.FILE_SIZE)
      .from(p)
      .innerJoin(ph)
      .on(p.FILE_HASH.eq(ph.HASH))
      .apply { libraryId?.let { innerJoin(b).on(b.ID.eq(p.BOOK_ID)) } }
      .where(ph.ACTION.`in`(actions))
      .apply { libraryId?.let { and(b.LIBRARY_ID.eq(it)) } }
      .fetch {
        it.value1() to
          BookPageNumbered(
            fileName = it.value2(),
            pageNumber = it.value3() + 1,
            fileHash = it.value4(),
            mediaType = it.value5(),
            fileSize = it.value6(),
          )
      }.groupingBy { it.first }
      .fold(emptyList()) { acc, (_, new) -> acc + new }

  override fun getKnownThumbnail(pageHash: String): ByteArray? =
    dsl
      .select(pht.THUMBNAIL)
      .from(pht)
      .where(pht.HASH.eq(pageHash))
      .fetchOne()
      ?.value1()

  @Transactional
  override fun insert(
    pageHash: PageHashKnown,
    thumbnail: ByteArray?,
  ) {
    dsl
      .insertInto(ph)
      .set(ph.HASH, pageHash.hash)
      .set(ph.SIZE, pageHash.size)
      .set(ph.ACTION, pageHash.action.name)
      .execute()

    if (thumbnail != null) {
      dsl
        .insertInto(pht)
        .set(pht.HASH, pageHash.hash)
        .set(pht.THUMBNAIL, thumbnail)
        .execute()
    }
  }

  override fun update(pageHash: PageHashKnown) {
    dsl
      .update(ph)
      .set(ph.ACTION, pageHash.action.name)
      .set(ph.SIZE, pageHash.size)
      .set(ph.DELETE_COUNT, pageHash.deleteCount)
      .set(ph.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(ph.HASH.eq(pageHash.hash))
      .execute()
  }
}

private fun PageHashRecord.toDomain(matchCount: Int = 0) =
  PageHashKnown(
    hash = hash,
    size = size,
    deleteCount = deleteCount,
    matchCount = matchCount,
    action = PageHashKnown.Action.valueOf(action),
    createdDate = createdDate.toCurrentTimeZone(),
    lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
  )
