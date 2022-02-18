package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.HistoricalEvent

interface HistoricalEventRepository {
  fun insert(event: HistoricalEvent)
}
