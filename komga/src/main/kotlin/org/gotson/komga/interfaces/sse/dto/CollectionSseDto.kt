package org.gotson.komga.interfaces.sse.dto

data class CollectionSseDto(
  val collectionId: String,
  val seriesIds: List<String>,
)
