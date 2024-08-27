package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class KoboSeriesDto(
  val id: String,
  val name: String,
  val number: String,
  val numberFloat: Float,
)
