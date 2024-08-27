package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class TagDto(
  val id: String,
  val created: ZonedDateTime,
  val lastModified: ZonedDateTime,
  val name: String,
  val type: TagTypeDto,
  val items: List<TagItemDto>? = null,
)
