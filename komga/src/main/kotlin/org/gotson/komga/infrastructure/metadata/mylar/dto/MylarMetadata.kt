package org.gotson.komga.infrastructure.metadata.mylar.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class AlternateTitleEntry(
  val title: String,
  val language: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MylarMetadata(
  val type: String = "comicSeries",
  val publisher: String = "Unknown",
  val imprint: String? = null,
  val name: String,
  @field:JsonAlias("cid")
  val comicid: String = "",
  val year: Int? = null,
  @field:JsonProperty("description_text")
  val descriptionText: String? = null,
  @field:JsonProperty("description_formatted")
  val descriptionFormatted: String? = null,
  val volume: Int? = null,
  @field:JsonProperty("booktype")
  val bookType: String = "print",
  @field:JsonProperty("age_rating")
  val ageRating: AgeRating? = null,
  @field:JsonProperty("comic_image")
  @field:JsonAlias("ComicImage")
  val comicImage: String? = null,
  @field:JsonProperty("total_issues")
  val totalIssues: Int? = null,
  @field:JsonProperty("publication_run")
  val publicationRun: String? = null,
  val status: Status? = null,
  // Custom fields for extended metadata support (e.g., from manga-py)
  @field:JsonProperty("alternate_titles")
  @field:JsonDeserialize(using = AlternateTitleDeserializer::class)
  val alternateTitles: List<AlternateTitleEntry>? = null,
  val genres: List<String>? = null,
  val tags: List<String>? = null,
  val authors: List<String>? = null,
  val artists: List<String>? = null,
  @field:JsonProperty("web_url")
  val webUrl: String? = null,
  @field:JsonProperty("content_rating")
  val contentRating: String? = null,
)
