package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.gotson.komga.domain.model.SyncPoint
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

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class WrappedTagDto(
  val tag: TagDto,
)

fun SyncPoint.ReadList.toWrappedTagDto(items: List<TagItemDto>? = null) =
  WrappedTagDto(
    TagDto(
      id = readListId,
      created = createdDate,
      lastModified = lastModifiedDate,
      name = readListName,
      type = TagTypeDto.USER_TAG,
      items = items,
    ),
  )
