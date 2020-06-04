package org.gotson.komga.domain.model

open class BookSearch(
  val libraryIds: Collection<Long> = emptyList(),
  val seriesIds: Collection<Long> = emptyList(),
  val searchTerm: String? = null,
  val mediaStatus: Collection<Media.Status> = emptyList()
)

class BookSearchWithReadProgress(
  libraryIds: Collection<Long> = emptyList(),
  seriesIds: Collection<Long> = emptyList(),
  searchTerm: String? = null,
  mediaStatus: Collection<Media.Status> = emptyList(),
  val readStatus: Collection<ReadStatus> = emptyList()
) : BookSearch(libraryIds, seriesIds, searchTerm, mediaStatus)

enum class ReadStatus {
  UNREAD, READ, IN_PROGRESS
}
