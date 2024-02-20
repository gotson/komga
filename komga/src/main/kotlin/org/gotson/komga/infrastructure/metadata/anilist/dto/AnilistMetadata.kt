package org.gotson.komga.infrastructure.metadata.anilist.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Root(
  val data: Data,
)

data class Data(
  @JsonProperty("Media")
  val media: Media,
)

data class Media(
  val description: String,
  val title: Title,
  val status: String,
  val startDate: StartDate,
  val genres: List<String>,
  val tags: List<Tag>,
  val countryOfOrigin: String,
  val coverImage: CoverImage,
  val isAdult: Boolean,
  val siteUrl: String,
  val volumes: Int?,
)

data class Title(
  val romaji: String,
  val english: String,
  val native: String,
  val userPreferred: String,
)

data class StartDate(
  val year: Long,
)

data class Tag(
  val name: String,
)

data class CoverImage(
  val extraLarge: String,
  val large: String,
  val medium: String,
)
