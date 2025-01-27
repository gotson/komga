package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

const val DUMMY_ID = "00000000-0000-0000-0000-000000000001"

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class KoboBookMetadataDto(
  val categories: List<String> = listOf(DUMMY_ID),
  val contributorRoles: List<ContributorDto> = emptyList(),
  val contributors: List<String> = emptyList(),
  val coverImageId: String? = null,
  val crossRevisionId: String,
  val currentDisplayPrice: AmountDto = AmountDto("USD", 0),
  val currentLoveDisplayPrice: AmountDto = AmountDto(totalAmount = 0),
  val description: String? = null,
  val downloadUrls: List<DownloadUrlDto> = emptyList(),
  val entitlementId: String,
  val externalIds: List<String> = emptyList(),
  val genre: String = DUMMY_ID,
  val isEligibleForKoboLove: Boolean = false,
  val isInternetArchive: Boolean = false,
  val isPreOrder: Boolean = false,
  val isSocialEnabled: Boolean = true,
  val isbn: String? = null,
  /**
   * 2-letter code
   */
  val language: String = "en",
  val phoneticPronunciations: Map<String, String> = emptyMap(),
  val publicationDate: ZonedDateTime? = null,
  val publisher: PublisherDto? = null,
  val revisionId: String,
  val series: KoboSeriesDto? = null,
  val slug: String? = null,
  val subTitle: String? = null,
  val title: String,
  val workId: String,
  @JsonIgnore
  val isKepub: Boolean,
  @JsonIgnore
  val isPrePaginated: Boolean,
  @JsonIgnore
  val fileSize: Long,
)
