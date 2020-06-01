package org.gotson.komga.domain.model

data class BookSearch(
  val libraryIds: Collection<Long> = emptyList(),
  val seriesIds: Collection<Long> = emptyList(),
  val searchTerm: String? = null,
  val mediaStatus: Collection<Media.Status> = emptyList()
)
