package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GithubReleaseDto(
  val htmlUrl: String,
  val tagName: String,
  val publishedAt: ZonedDateTime,
  val body: String,
  val prerelease: Boolean,
)
