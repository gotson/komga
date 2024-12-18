package org.gotson.komga.interfaces.api.rest.dto

import java.time.ZonedDateTime

data class ReleaseDto(
  val version: String,
  val releaseDate: ZonedDateTime,
  val url: String,
  val latest: Boolean,
  val preRelease: Boolean,
  val description: String,
)
