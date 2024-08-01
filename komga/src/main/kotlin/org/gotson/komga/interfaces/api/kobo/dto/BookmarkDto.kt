package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class BookmarkDto(
  val lastModified: ZonedDateTime,
  val progressPercent: Float? = null,
  val contentSourceProgressPercent: Float? = null,
  val location: LocationDto? = null,
)
