package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.PageHash
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.model.PageHashMatch
import org.gotson.komga.domain.model.PageHashUnknown
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.jooq.Tables
import org.gotson.komga.jooq.tables.records.PageHashRecord
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

  private val sortsKnown = mapOf(
    "hash" to ph.HASH,
    "mediatype" to ph.MEDIA_TYPE,
    "fileSize" to ph.SIZE,
    "deleteCount" to ph.DELETE_COUNT,
    "deleteSize" to ph.SIZE * ph.DELETE_COUNT,
  )

  private val sortsUnknown = mapOf(
    "hash" to p.FILE_HASH,
    "mediatype" to p.MEDIA_TYPE,
    "fileSize" to p.FILE_SIZE,
    "matchCount" to DSL.field("count"),
    "totalSize" to DSL.field("totalSize"),
    "url" to b.URL,
    "bookId" to b.ID,
    "pageNumber" to p.NUMBER,
  )

  override fun findKnown(pageHash: PageHash): PageHashKnown? =
    dsl.selectFrom(ph)
      .where(ph.HASH.eq(pageHash.hash))
      .and(ph.MEDIA_TYPE.eq(pageHash.mediaType))
      .apply {
        if (pageHash.size == null) and(ph.SIZE.isNull)
        else and(ph.SIZE.eq(pageHash.size))
      }
      .fetchOneInto(ph)
      ?.toDomain()

  override fun findAllKnown(actions: List<PageHashKnown.Action>?, pageable: Pageable): Page<PageHashKnown> {
    val query = dsl.selectFrom(ph)
      .apply { actions?.let { where(ph.ACTION.`in`(actions)) } }

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sortsKnown)
    val items = query
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetch()
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findAllUnknown(pageable: Pageable): Page<PageHashUnknown> {
    val bookCount = DSL.count(p.BOOK_ID)
    val query = dsl.select(
      p.FILE_HASH,
      p.MEDIA_TYPE,
      p.FILE_SIZE,
      bookCount.`as`("count"),
      (bookCount * p.FILE_SIZE).`as`("totalSize"),
    )
      .from(p)
      .where(p.FILE_HASH.ne(""))
      .and(
        DSL.notExists(
          dsl.selectOne()
            .from(ph)
            .where(ph.HASH.eq(p.FILE_HASH))
            .and(ph.MEDIA_TYPE.eq(p.MEDIA_TYPE))
            .and(
              ph.SIZE.eq(p.FILE_SIZE).or(
                ph.SIZE.isNull.and(p.FILE_SIZE.isNull),
              ),
            ),
        ),
      )
      .groupBy(p.FILE_HASH, p.MEDIA_TYPE, p.FILE_SIZE)
      .having(DSL.count(p.BOOK_ID).gt(1))

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sortsUnknown)
    val items = query
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetch {
        PageHashUnknown(it.value1(), it.value2(), it.value3(), it.value4())
      }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findMatchesByHash(pageHash: PageHash, libraryId: String?, pageable: Pageable): Page<PageHashMatch> {
    val query = dsl.select(p.BOOK_ID, b.URL, p.NUMBER, p.FILE_NAME)
      .from(p)
      .leftJoin(b).on(p.BOOK_ID.eq(b.ID))
      .where(p.FILE_HASH.eq(pageHash.hash))
      .and(p.MEDIA_TYPE.eq(pageHash.mediaType))
      .apply {
        if (pageHash.size == null) and(p.FILE_SIZE.isNull)
        else and(p.FILE_SIZE.eq(pageHash.size))
      }
      .apply { libraryId?.let { and(b.LIBRARY_ID.eq(it)) } }

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sortsUnknown)
    val items = query
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetch {
        PageHashMatch(
          bookId = it.value1(),
          url = URL(it.value2()),
          pageNumber = it.value3() + 1,
          fileName = it.value4(),
        )
      }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun getKnownThumbnail(pageHash: PageHash): ByteArray? =
    dsl.select(pht.THUMBNAIL)
      .from(pht)
      .where(pht.HASH.eq(pageHash.hash))
      .and(pht.MEDIA_TYPE.eq(pageHash.mediaType))
      .apply {
        if (pageHash.size == null) and(pht.SIZE.isNull)
        else and(pht.SIZE.eq(pageHash.size))
      }
      .fetchOne()?.value1()

  @Transactional
  override fun insert(pageHash: PageHashKnown, thumbnail: ByteArray?) {
    dsl.insertInto(ph)
      .set(ph.HASH, pageHash.hash)
      .set(ph.MEDIA_TYPE, pageHash.mediaType)
      .set(ph.SIZE, pageHash.size)
      .set(ph.ACTION, pageHash.action.name)
      .execute()

    if (thumbnail != null) {
      dsl.insertInto(pht)
        .set(pht.HASH, pageHash.hash)
        .set(pht.MEDIA_TYPE, pageHash.mediaType)
        .set(pht.SIZE, pageHash.size)
        .set(pht.THUMBNAIL, thumbnail)
        .execute()
    }
  }

  override fun update(pageHash: PageHashKnown) {
    dsl.update(ph)
      .set(ph.ACTION, pageHash.action.name)
      .set(ph.DELETE_COUNT, pageHash.deleteCount)
      .set(ph.LAST_MODIFIED_DATE, LocalDateTime.now(ZoneId.of("Z")))
      .where(ph.HASH.eq(pageHash.hash))
      .and(ph.MEDIA_TYPE.eq(pageHash.mediaType))
      .apply {
        if (pageHash.size == null) and(ph.SIZE.isNull)
        else and(ph.SIZE.eq(pageHash.size))
      }
      .execute()
  }
}

private fun PageHashRecord.toDomain() =
  PageHashKnown(
    hash = hash,
    mediaType = mediaType,
    size = size,
    deleteCount = deleteCount,
    action = PageHashKnown.Action.valueOf(action),
    createdDate = createdDate.toCurrentTimeZone(),
    lastModifiedDate = lastModifiedDate.toCurrentTimeZone(),
  )
