package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class RequestResultDto(
  val requestResult: ResultDto,
  val updateResults: Collection<UpdateResultDto>,
)

interface UpdateResultDto

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class ReadingStateUpdateResultDto(
  val entitlementId: String,
  val currentBookmarkResult: WrappedResultDto,
  val statisticsResult: WrappedResultDto,
  val statusInfoResult: WrappedResultDto,
) : UpdateResultDto

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class WrappedResultDto(
  val result: ResultDto,
)
