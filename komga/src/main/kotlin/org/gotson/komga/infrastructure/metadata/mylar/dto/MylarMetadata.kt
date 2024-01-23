package org.gotson.komga.infrastructure.metadata.mylar.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MylarMetadata(
  val type: String,
  val publisher: String,
  val imprint: String?,
  val name: String,
  @field:JsonAlias("cid")
  val comicid: String,
  val year: Int,
  @field:JsonProperty("description_text")
  val descriptionText: String?,
  @field:JsonProperty("description_formatted")
  val descriptionFormatted: String?,
  val volume: Int?,
  @field:JsonProperty("booktype")
  val bookType: String,
  @field:JsonProperty("age_rating")
  val ageRating: AgeRating?,
  @field:JsonProperty("comic_image")
  @field:JsonAlias("ComicImage")
  val comicImage: String,
  @field:JsonProperty("total_issues")
  val totalIssues: Int,
  @field:JsonProperty("publication_run")
  val publicationRun: String,
  val status: Status,
)
