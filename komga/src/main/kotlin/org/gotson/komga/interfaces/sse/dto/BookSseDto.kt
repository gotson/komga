package org.gotson.komga.interfaces.sse.dto

data class BookSseDto(
  val bookId: String,
  val seriesId: String,
  val libraryId: String,
)
