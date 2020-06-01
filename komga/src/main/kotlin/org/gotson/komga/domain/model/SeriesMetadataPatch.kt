package org.gotson.komga.domain.model

data class SeriesMetadataPatch(
  val title: String?,
  val titleSort: String?,
  val status: SeriesMetadata.Status?
)
