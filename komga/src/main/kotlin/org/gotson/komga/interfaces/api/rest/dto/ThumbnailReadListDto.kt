package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ThumbnailReadList

data class ThumbnailReadListDto(
  val id: String,
  val readListId: String,
  val type: String,
  val selected: Boolean,
)

fun ThumbnailReadList.toDto() =
  ThumbnailReadListDto(
    id = id,
    readListId = readListId,
    type = type.toString(),
    selected = selected,
  )
