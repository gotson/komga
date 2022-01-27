package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.PageHash
import org.gotson.komga.domain.model.PageHashMatch
import org.gotson.komga.domain.model.PageHashUnknown
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.jooq.Tables
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.net.URL

@Component
class PageHashDao(
  private val dsl: DSLContext,
) : PageHashRepository {

  private val p = Tables.MEDIA_PAGE
  private val b = Tables.BOOK

  private val sorts = mapOf(
    "hash" to p.FILE_HASH,
    "mediatype" to p.MEDIA_TYPE,
    "size" to DSL.field("size"),
    "matchCount" to DSL.field("count"),
    "url" to b.URL,
    "bookId" to b.ID,
    "pageNumber" to p.NUMBER,
  )

  override fun findAllKnown(actions: List<PageHash.Action>?, pageable: Pageable): Page<PageHash> {
    TODO("Not yet implemented")
  }

  override fun findAllUnknown(pageable: Pageable): Page<PageHashUnknown> {
    val query = dsl.select(
      p.FILE_HASH,
      p.MEDIA_TYPE,
      p.FILE_SIZE,
      DSL.count(p.BOOK_ID).`as`("count"),
    )
      .from(p)
      .where(p.FILE_HASH.ne(""))
      .groupBy(p.FILE_HASH, p.MEDIA_TYPE, p.FILE_SIZE)
      .having(DSL.count(p.BOOK_ID).gt(1))

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sorts)
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

  override fun findMatchesByHash(pageHash: PageHashUnknown, pageable: Pageable): Page<PageHashMatch> {
    val query = dsl.select(p.BOOK_ID, b.URL, p.NUMBER, p.FILE_NAME)
      .from(p)
      .leftJoin(b).on(p.BOOK_ID.eq(b.ID))
      .where(p.FILE_HASH.eq(pageHash.hash))
      .and(p.MEDIA_TYPE.eq(pageHash.mediaType))
      .apply {
        if (pageHash.size == null) and(p.FILE_SIZE.isNull)
        else and(p.FILE_SIZE.eq(pageHash.size))
      }

    val count = dsl.fetchCount(query)

    val orderBy = pageable.sort.toOrderBy(sorts)
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

  override fun getKnownThumbnail(hash: String): ByteArray? {
    TODO("Not yet implemented")
  }
}
