package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.ThumbnailSeries

data class SeriesThumbnailDto(
  val id: String,
  val seriesId: String,
  val type: String,
  val selected: Boolean,
)

fun ThumbnailSeries.toDto() =
  SeriesThumbnailDto(
    id = id,
    seriesId = seriesId,
    type = type.toString(),
    selected = selected,
  )
