package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.domain.model.SeriesMetadata

data class SeriesMetadataUpdateDto(
  val status: SeriesMetadata.Status?,
  val statusLock: Boolean?,
  val title: String?,
  val titleLock: Boolean?,
  val titleSort: String?,
  val titleSortLock: Boolean?
)
