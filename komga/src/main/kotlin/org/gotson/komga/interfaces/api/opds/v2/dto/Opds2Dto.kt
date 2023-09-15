package org.gotson.komga.interfaces.api.opds.v2.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.gotson.komga.interfaces.api.dto.WPLinkDto
import org.gotson.komga.interfaces.api.dto.WPPublicationDto
import org.springframework.data.domain.Page
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class FeedDto(
  val metadata: FeedMetadataDto,
  val links: List<WPLinkDto> = emptyList(),
  val navigation: List<WPLinkDto> = emptyList(),
  val facets: List<FacetDto> = emptyList(),
  val groups: List<FeedGroupDto> = emptyList(),
  val publications: List<WPPublicationDto> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class FacetDto(
  val metadata: FeedMetadataDto,
  val links: List<WPLinkDto> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class FeedGroupDto(
  val metadata: FeedMetadataDto,
  val links: List<WPLinkDto> = emptyList(),
  val navigation: List<WPLinkDto> = emptyList(),
  val publications: List<WPPublicationDto> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class FeedMetadataDto(
  val title: String,
  val subTitle: String? = null,
  @JsonAlias("@type")
  val type: String? = null,
  val identifier: String? = null,
  val modified: ZonedDateTime? = null,
  val description: String? = null,
  @JsonIgnore
  val page: Page<*>? = null,
  @Positive
  val itemsPerPage: Int? = page?.size,
  @Positive
  val currentPage: Int? = page?.number?.plus(1),
  @PositiveOrZero
  val numberOfItems: Long? = page?.totalElements,
)
