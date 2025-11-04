package org.gotson.komga.interfaces.api.rest.dto

data class ContentBlacklistDto(
  val tagsExclude: Set<String> = emptySet(),
  val genresExclude: Set<String> = emptySet(),
)

data class ContentBlacklistUpdateDto(
  val tagsExclude: Set<String>? = null,
  val genresExclude: Set<String>? = null,
)

data class FilterSummaryDto(
  val hasRestrictions: Boolean,
  val tagsBlacklisted: Int,
  val genresBlacklisted: Int,
  val ageRestricted: Boolean,
  val labelsRestricted: Boolean,
)

data class AddToBlacklistDto(
  val type: String, // "tag" or "genre"
  val value: String,
)

data class RemoveFromBlacklistDto(
  val type: String, // "tag" or "genre"
  val value: String,
)
