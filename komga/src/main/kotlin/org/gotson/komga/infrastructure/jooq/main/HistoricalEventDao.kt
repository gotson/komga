package org.gotson.komga.infrastructure.jooq.main

import org.gotson.komga.domain.model.HistoricalEvent
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.gotson.komga.jooq.main.Tables
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class HistoricalEventDao(
  private val dsl: DSLContext,
) : HistoricalEventRepository {
  private val e = Tables.HISTORICAL_EVENT
  private val ep = Tables.HISTORICAL_EVENT_PROPERTIES

  @Transactional
  override fun insert(event: HistoricalEvent) {
    dsl
      .insertInto(e)
      .set(e.ID, event.id)
      .set(e.TYPE, event.type)
      .set(e.BOOK_ID, event.bookId)
      .set(e.SERIES_ID, event.seriesId)
      .set(e.TIMESTAMP, event.timestamp)
      .execute()

    if (event.properties.isNotEmpty()) {
      dsl
        .batch(
          dsl
            .insertInto(ep, ep.ID, ep.KEY, ep.VALUE)
            .values(null as String?, null, null),
        ).also { step ->
          event.properties.forEach { (key, value) ->
            step.bind(event.id, key, value)
          }
        }.execute()
    }
  }
}
