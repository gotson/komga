package org.gotson.komga.domain.model

open class BookSearch(
  val libraryIds: Collection<Long>? = null,
  val seriesIds: Collection<Long>? = null,
  val searchTerm: String? = null,
  val mediaStatus: Collection<Media.Status>? = null
)

class BookSearchWithReadProgress(
  libraryIds: Collection<Long>? = null,
  seriesIds: Collection<Long>? = null,
  searchTerm: String? = null,
  mediaStatus: Collection<Media.Status>? = null,
  val readStatus: Collection<ReadStatus>? = null
) : BookSearch(libraryIds, seriesIds, searchTerm, mediaStatus)

