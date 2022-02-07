package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.PageHashUnknown

data class PageHashUnknownDto(
  val hash: String,
  val mediaType: String,
  val size: Long?,
  val matchCount: Int,
)

fun PageHashUnknown.toDto() = PageHashUnknownDto(
  hash = hash,
  mediaType = mediaType,
  size = size,
  matchCount = matchCount,
)
