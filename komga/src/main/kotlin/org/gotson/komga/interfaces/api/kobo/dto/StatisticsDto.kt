package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class StatisticsDto(
  val lastModified: ZonedDateTime,
  val remainingTimeMinutes: Int? = null,
  val spentReadingMinutes: Int? = null,
)
