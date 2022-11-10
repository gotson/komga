package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.constraints.PositiveOrZero

data class TachiyomiReadProgressUpdateDto(
  @get:PositiveOrZero val lastBookRead: Int,
)

data class TachiyomiReadProgressUpdateV2Dto(
  val lastBookNumberSortRead: Float,
)
