package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.infrastructure.validation.NullOrNotBlank

data class SeriesMetadataUpdateDto(
  val status: SeriesMetadata.Status?,
  val statusLock: Boolean?,
  @get:NullOrNotBlank
  val title: String?,
  val titleLock: Boolean?,
  @get:NullOrNotBlank
  val titleSort: String?,
  val titleSortLock: Boolean?
)
