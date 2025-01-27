package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.gotson.komga.domain.model.ReadProgress
import org.gotson.komga.language.toUTCZoned
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class ReadingStateDto(
  val created: ZonedDateTime? = null,
  val currentBookmark: BookmarkDto,
  val entitlementId: String,
  val lastModified: ZonedDateTime,
  /**
   * From CW: apparently always equals to lastModified
   */
  val priorityTimestamp: ZonedDateTime? = null,
  val statistics: StatisticsDto,
  val statusInfo: StatusInfoDto,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class WrappedReadingStateDto(
  val readingState: ReadingStateDto,
)

fun ReadProgress.toDto() =
  ReadingStateDto(
    created = this.createdDate.toUTCZoned(),
    lastModified = this.lastModifiedDate.toUTCZoned(),
    priorityTimestamp = this.lastModifiedDate.toUTCZoned(),
    entitlementId = this.bookId,
    currentBookmark =
      BookmarkDto(
        lastModified = this.lastModifiedDate.toUTCZoned(),
        progressPercent =
          this.locator
            ?.locations
            ?.totalProgression
            ?.times(100),
        contentSourceProgressPercent =
          this.locator
            ?.locations
            ?.progression
            ?.times(100),
        location = this.locator?.let { LocationDto(source = it.href, value = it.koboSpan) },
      ),
    statistics =
      StatisticsDto(
        lastModified = this.lastModifiedDate.toUTCZoned(),
      ),
    statusInfo =
      StatusInfoDto(
        lastModified = this.lastModifiedDate.toUTCZoned(),
        status =
          when {
            this.completed -> StatusDto.FINISHED
            !this.completed -> StatusDto.READING
            else -> StatusDto.READY_TO_READ
          },
        timesStartedReading = 1,
      ),
  )
