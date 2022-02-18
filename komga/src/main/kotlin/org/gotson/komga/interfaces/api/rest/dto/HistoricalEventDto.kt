package org.gotson.komga.interfaces.api.rest.dto

import java.time.LocalDateTime

data class HistoricalEventDto(
  val type: String,
  val timestamp: LocalDateTime,
  val bookId: String?,
  val seriesId: String?,
  val properties: Map<String, String>,
)
