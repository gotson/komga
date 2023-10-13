package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ThumbnailSeriesCollection

data class ThumbnailSeriesCollectionDto(
  val id: String,
  val collectionId: String,
  val type: String,
  val selected: Boolean,
  val mediaType: String,
  val fileSize: Long,
  val width: Int,
  val height: Int,
)

fun ThumbnailSeriesCollection.toDto() =
  ThumbnailSeriesCollectionDto(
    id = id,
    collectionId = collectionId,
    type = type.toString(),
    selected = selected,
    mediaType = mediaType,
    fileSize = fileSize,
    width = dimension.width,
    height = dimension.height,
  )
