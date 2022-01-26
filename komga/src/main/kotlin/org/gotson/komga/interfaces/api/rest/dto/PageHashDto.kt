package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.PageHash
import java.time.LocalDateTime

data class PageHashDto(
  val hash: String,
  val mediaType: String,
  val size: Long?,
  val action: PageHash.Action,
  val deleteCount: Int,

  val created: LocalDateTime,
  val lastModified: LocalDateTime,
)

fun PageHash.toDto() = PageHashDto(
  hash = hash,
  mediaType = mediaType,
  size = size,
  action = action,
  deleteCount = deleteCount,
  created = createdDate,
  lastModified = lastModifiedDate,
)
