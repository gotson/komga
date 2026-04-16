package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class StatusInfoDto(
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  val lastModified: ZonedDateTime,
  val status: StatusDto,
  val timesStartedReading: Int? = null,
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  val lastTimeFinished: ZonedDateTime? = null,
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  val lastTimeStartedReading: ZonedDateTime? = null,
)
