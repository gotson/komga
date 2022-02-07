package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.PageHashKnown
import java.time.LocalDateTime

data class PageHashKnownDto(
  val hash: String,
  val mediaType: String,
  val size: Long?,
  val action: PageHashKnown.Action,
  val deleteCount: Int,

  val created: LocalDateTime,
  val lastModified: LocalDateTime,
)

fun PageHashKnown.toDto() = PageHashKnownDto(
  hash = hash,
  mediaType = mediaType,
  size = size,
  action = action,
  deleteCount = deleteCount,
  created = createdDate,
  lastModified = lastModifiedDate,
)
