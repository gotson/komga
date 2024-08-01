package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class ReadingStateDto(
  val created: ZonedDateTime,
  val currentBookmark: BookmarkDto,
  val entitlementId: String,
  val lastModified: ZonedDateTime,
  /**
   * From CW: apparently always equals to lastModified
   */
  val priorityTimestamp: ZonedDateTime,
  val statistics: StatisticsDto,
  val statusInfo: StatusInfoDto,
)
