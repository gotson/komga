package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

interface SyncResultDto

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class NewEntitlementDto(
  val newEntitlement: BookEntitlementContainerDto,
) : SyncResultDto

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class ChangedEntitlementDto(
  val changedEntitlement: BookEntitlementContainerDto,
) : SyncResultDto

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class NewTagDto(
  val newTag: TagDto,
) : SyncResultDto

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class ChangedTagDto(
  val changedTag: TagDto,
) : SyncResultDto

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class ChangedReadingStateDto(
  val changedReadingState: WrappedReadingStateDto,
) : SyncResultDto
