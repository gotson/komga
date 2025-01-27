package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class LocationDto(
  /**
   * For type=KoboSpan values are in the form "kobo.x.y"
   */
  val value: String? = null,
  /**
   * Typically "KoboSpan"
   */
  val type: String? = "KoboSpan",
  /**
   * The epub HTML resource
   */
  val source: String,
)
