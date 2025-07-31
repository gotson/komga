package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.infrastructure.jooq.toOrderBy
import org.gotson.komga.interfaces.api.persistence.HistoricalEventDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.HistoricalEventDto
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class HistoricalEventDtoDao(
  @Qualifier("dslContextRO") private val dslRO: DSLContext,
) : HistoricalEventDtoRepository {
  private val e = Tables.HISTORICAL_EVENT
  private val ep = Tables.HISTORICAL_EVENT_PROPERTIES

  private val sorts =
    mapOf(
      "type" to e.TYPE,
      "bookId" to e.BOOK_ID,
      "seriesId" to e.SERIES_ID,
      "timestamp" to e.TIMESTAMP,
    )

  override fun findAll(pageable: Pageable): Page<HistoricalEventDto> {
    val count = dslRO.fetchCount(e)

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items =
      dslRO
        .selectFrom(e)
        .orderBy(orderBy)
        .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
        .map { er ->
          val epr = dslRO.selectFrom(ep).where(ep.ID.eq(er.id)).fetch()
          HistoricalEventDto(
            type = er.type,
            timestamp = er.timestamp,
            bookId = er.bookId,
            seriesId = er.seriesId,
            properties = epr.filterNot { it.key == null }.associate { it.key to it.value },
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
}
