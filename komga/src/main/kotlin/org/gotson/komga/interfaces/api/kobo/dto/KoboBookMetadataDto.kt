package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.ZonedDateTime

const val DUMMY_ID = "00000000-0000-0000-0000-000000000001"

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class KoboBookMetadataDto(
  val categories: List<String> = listOf(DUMMY_ID),
  val contributorRoles: List<ContributorDto>,
  val contributors: List<String>,
  val coverImageId: String,
  val crossRevisionId: String,
  val currentDisplayPrice: AmountDto = AmountDto("USD", 0),
  val currentLoveDisplayPrice: AmountDto = AmountDto(totalAmount = 0),
  val description: String,
  val downloadUrls: List<DownloadUrlDto>,
  val entitlementId: String,
  val externalIds: List<String> = emptyList(),
  val genre: String = DUMMY_ID,
  val isEligibleForKoboLove: Boolean = false,
  val isInternetArchive: Boolean = false,
  val isPreOrder: Boolean = false,
  val isSocialEnabled: Boolean = false,
  val isbn: String? = null,
  /**
   * 2-letter code
   */
  val language: String,
  val phoneticPronunciations: Map<String, String> = emptyMap(),
  val publicationDate: ZonedDateTime,
  val publisher: PublisherDto,
  val revisionId: String,
  val series: KoboSeriesDto?,
  val slug: String? = null,
  val subTitle: String? = null,
  val title: String,
  val workId: String,
)
