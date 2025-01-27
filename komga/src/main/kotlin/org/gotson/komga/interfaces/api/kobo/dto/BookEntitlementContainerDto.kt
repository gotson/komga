package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class BookEntitlementContainerDto(
  val bookEntitlement: BookEntitlementDto,
  val bookMetadata: KoboBookMetadataDto,
  val readingState: ReadingStateDto? = null,
)
