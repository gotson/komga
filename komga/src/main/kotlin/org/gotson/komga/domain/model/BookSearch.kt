package org.gotson.komga.domain.model

import java.time.LocalDate

open class BookSearch(
  val libraryIds: Collection<String>? = null,
  val seriesIds: Collection<String>? = null,
  val searchTerm: String? = null,
  val mediaStatus: Collection<Media.Status>? = null,
  val deleted: Boolean? = null,
  val releasedAfter: LocalDate? = null,
)

class BookSearchWithReadProgress(
  libraryIds: Collection<String>? = null,
  seriesIds: Collection<String>? = null,
  searchTerm: String? = null,
  mediaStatus: Collection<Media.Status>? = null,
  deleted: Boolean? = null,
  releasedAfter: LocalDate? = null,
  val tags: Collection<String>? = null,
  val readStatus: Collection<ReadStatus>? = null,
  val authors: Collection<Author>? = null,
) : BookSearch(
    libraryIds = libraryIds,
    seriesIds = seriesIds,
    searchTerm = searchTerm,
    mediaStatus = mediaStatus,
    deleted = deleted,
    releasedAfter = releasedAfter,
  )
