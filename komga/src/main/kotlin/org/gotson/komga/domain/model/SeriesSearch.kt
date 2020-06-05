package org.gotson.komga.domain.model

open class SeriesSearch(
  val libraryIds: Collection<Long> = emptyList(),
  val searchTerm: String? = null,
  val metadataStatus: Collection<SeriesMetadata.Status> = emptyList()
)

class SeriesSearchWithReadProgress(
  libraryIds: Collection<Long> = emptyList(),
  searchTerm: String? = null,
  metadataStatus: Collection<SeriesMetadata.Status> = emptyList(),
  val readStatus: Collection<ReadStatus> = emptyList()
) : SeriesSearch(libraryIds, searchTerm, metadataStatus)
