package org.gotson.komga.interfaces.api.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WPLinkDto(
  val title: String? = null,
  val rel: String? = null,
  val href: String? = null,
  val type: String? = null,
  val templated: Boolean? = null,
  @Positive
  val width: Int? = null,
  @Positive
  val height: Int? = null,
  val alternate: List<WPLinkDto> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WPPublicationDto(
  val metadata: WPMetadataDto,
  val links: List<WPLinkDto>,
  val images: List<WPLinkDto>,
  val readingOrder: List<WPLinkDto>,
  val resources: List<WPLinkDto> = emptyList(),
  val toc: List<WPLinkDto> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WPMetadataDto(
  val title: String,
  val identifier: String? = null,
  @JsonAlias("@type")
  val type: String? = null,
  val conformsTo: String? = null,
  val sortAs: String? = null,
  val subtitle: String? = null,
  val modified: ZonedDateTime? = null,
  val published: LocalDate? = null,
  val language: String? = null,
  val author: List<String> = emptyList(),
  val translator: List<String> = emptyList(),
  val editor: List<String> = emptyList(),
  val artist: List<String> = emptyList(),
  val illustrator: List<String> = emptyList(),
  val letterer: List<String> = emptyList(),
  val penciler: List<String> = emptyList(),
  val colorist: List<String> = emptyList(),
  val inker: List<String> = emptyList(),
  val contributor: List<String> = emptyList(),
  val publisher: List<String> = emptyList(),
  val subject: List<String> = emptyList(),
  val readingProgression: WPReadingProgressionDto? = null,
  val description: String? = null,
  @Positive
  val numberOfPages: Int? = null,
  val belongsTo: WPBelongsToDto? = null,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WPBelongsToDto(
  val series: List<WPContributorDto> = emptyList(),
  val collection: List<WPContributorDto> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class WPContributorDto(
  val name: String,
  val position: Float? = null,
)

enum class WPReadingProgressionDto {
  @JsonProperty("rtl")
  RTL,

  @JsonProperty("ltr")
  LTR,

  @JsonProperty("ttb")
  TTB,

  @JsonProperty("btt")
  BTT,

  @JsonProperty("auto")
  AUTO,
}
