package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.domain.model.ThumbnailSeries

data class SeriesThumbnailDto(
  val id: String,
  val seriesId: String,
  val type: ThumbnailSeries.Type,
  val selected: Boolean
)
