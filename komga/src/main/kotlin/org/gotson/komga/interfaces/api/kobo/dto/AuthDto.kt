package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class AuthDto(
  val accessToken: String,
  val refreshToken: String,
  val tokenType: String = "Bearer",
  val trackingId: String,
  val userKey: String,
)
