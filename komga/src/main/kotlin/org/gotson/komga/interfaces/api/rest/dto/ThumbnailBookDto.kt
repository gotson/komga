package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ThumbnailBook

data class ThumbnailBookDto(
  val id: String,
  val bookId: String,
  val type: String,
  val selected: Boolean,
  val mediaType: String,
  val fileSize: Long,
  val width: Int,
  val height: Int,
)

fun ThumbnailBook.toDto() =
  ThumbnailBookDto(
    id = id,
    bookId = bookId,
    type = type.toString(),
    selected = selected,
    mediaType = mediaType,
    fileSize = fileSize,
    width = dimension.width,
    height = dimension.height,
  )
