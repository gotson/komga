package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class StatusInfoDto(
  val lastModified: ZonedDateTime,
  val status: StatusDto,
  val timesStartedReading: Int,
  val lastTimeFinished: ZonedDateTime? = null,
)
