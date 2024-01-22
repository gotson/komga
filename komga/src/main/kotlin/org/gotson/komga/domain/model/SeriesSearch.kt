package org.gotson.komga.domain.model

open class SeriesSearch(
  val libraryIds: Collection<String>? = null,
  val collectionIds: Collection<String>? = null,
  val searchTerm: String? = null,
  val searchRegex: Pair<String, SearchField>? = null,
  val metadataStatus: Collection<SeriesMetadata.Status>? = null,
  val publishers: Collection<String>? = null,
  val deleted: Boolean? = null,
  val complete: Boolean? = null,
  val oneshot: Boolean? = null,
) {
  enum class SearchField {
    NAME,
    TITLE,
    TITLE_SORT,
  }
}

class SeriesSearchWithReadProgress(
  libraryIds: Collection<String>? = null,
  collectionIds: Collection<String>? = null,
  searchTerm: String? = null,
  searchRegex: Pair<String, SearchField>? = null,
  metadataStatus: Collection<SeriesMetadata.Status>? = null,
  publishers: Collection<String>? = null,
  deleted: Boolean? = null,
  complete: Boolean? = null,
  oneshot: Boolean? = null,
  val languages: Collection<String>? = null,
  val genres: Collection<String>? = null,
  val tags: Collection<String>? = null,
  val ageRatings: Collection<Int?>? = null,
  val releaseYears: Collection<String>? = null,
  val readStatus: Collection<ReadStatus>? = null,
  val authors: Collection<Author>? = null,
  val sharingLabels: Collection<String>? = null,
) : SeriesSearch(
    libraryIds = libraryIds,
    collectionIds = collectionIds,
    searchTerm = searchTerm,
    searchRegex = searchRegex,
    metadataStatus = metadataStatus,
    publishers = publishers,
    deleted = deleted,
    complete = complete,
    oneshot = oneshot,
  )
