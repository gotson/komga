package org.gotson.komga.interfaces.api.rest.dto

import javax.validation.constraints.PositiveOrZero

data class TachiyomiReadProgressUpdateDto(
  @get:PositiveOrZero val lastBookRead: Int,
)

data class TachiyomiReadProgressUpdateV2Dto(
  val lastBookNumberSortRead: Float,
)
