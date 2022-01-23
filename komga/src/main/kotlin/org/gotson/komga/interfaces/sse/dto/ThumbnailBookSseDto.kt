package org.gotson.komga.interfaces.sse.dto

data class ThumbnailBookSseDto(
  val bookId: String,
  val seriesId: String,
  val selected: Boolean,
)
