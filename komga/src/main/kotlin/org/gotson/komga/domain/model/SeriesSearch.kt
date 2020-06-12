package org.gotson.komga.domain.model

open class SeriesSearch(
  val libraryIds: Collection<Long>? = null,
  val searchTerm: String? = null,
  val metadataStatus: Collection<SeriesMetadata.Status>? = null
)

class SeriesSearchWithReadProgress(
  libraryIds: Collection<Long>? = null,
  searchTerm: String? = null,
  metadataStatus: Collection<SeriesMetadata.Status>? = null,
  val readStatus: Collection<ReadStatus>? = null
) : SeriesSearch(libraryIds, searchTerm, metadataStatus)
