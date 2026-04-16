package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BookmarkDto(
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  val lastModified: ZonedDateTime,
  /**
   * Total progression in the book.
   * Between 0 and 100.
   */
  val progressPercent: Int? = null,
  /**
   * Progression within the resource.
   * Between 0 and 100.
   */
  val contentSourceProgressPercent: Int? = null,
  val location: LocationDto? = null,
)
