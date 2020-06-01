package org.gotson.komga.domain.model

data class SeriesSearch(
  val libraryIds: Collection<Long> = emptyList(),
  val searchTerm: String? = null,
  val metadataStatus: Collection<SeriesMetadata.Status> = emptyList()
)
