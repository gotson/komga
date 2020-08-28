package org.gotson.komga.domain.model

open class SeriesSearch(
  val libraryIds: Collection<String>? = null,
  val collectionIds: Collection<Long>? = null,
  val searchTerm: String? = null,
  val metadataStatus: Collection<SeriesMetadata.Status>? = null
)

class SeriesSearchWithReadProgress(
  libraryIds: Collection<String>? = null,
  collectionIds: Collection<Long>? = null,
  searchTerm: String? = null,
  metadataStatus: Collection<SeriesMetadata.Status>? = null,
  val publishers: Collection<String>? = null,
  val languages: Collection<String>? = null,
  val genres: Collection<String>? = null,
  val tags: Collection<String>? = null,
  val ageRatings: Collection<Int?>? = null,
  val readStatus: Collection<ReadStatus>? = null
) : SeriesSearch(
  libraryIds = libraryIds,
  collectionIds = collectionIds,
  searchTerm = searchTerm,
  metadataStatus = metadataStatus
)
