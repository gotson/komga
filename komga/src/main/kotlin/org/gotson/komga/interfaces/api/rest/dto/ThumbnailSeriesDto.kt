package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ThumbnailSeries

data class ThumbnailSeriesDto(
  val id: String,
  val seriesId: String,
  val type: String,
  val selected: Boolean,
  val mediaType: String,
  val fileSize: Long,
  val width: Int,
  val height: Int,
)

fun ThumbnailSeries.toDto() =
  ThumbnailSeriesDto(
    id = id,
    seriesId = seriesId,
    type = type.toString(),
    selected = selected,
    mediaType = mediaType,
    fileSize = fileSize,
    width = dimension.width,
    height = dimension.height,
  )
