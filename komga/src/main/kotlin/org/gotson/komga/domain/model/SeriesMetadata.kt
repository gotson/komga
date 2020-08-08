package org.gotson.komga.domain.model

import java.time.LocalDateTime

class SeriesMetadata(
  val status: Status = Status.ONGOING,
  title: String,
  titleSort: String = title,

  val statusLock: Boolean = false,
  val titleLock: Boolean = false,
  val titleSortLock: Boolean = false,

  val seriesId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {
  val title = title.trim()
  val titleSort = titleSort.trim()

  fun copy(
    status: Status = this.status,
    title: String = this.title,
    titleSort: String = this.titleSort,
    statusLock: Boolean = this.statusLock,
    titleLock: Boolean = this.titleLock,
    titleSortLock: Boolean = this.titleSortLock,
    seriesId: String = this.seriesId,
    createdDate: LocalDateTime = this.createdDate,
    lastModifiedDate: LocalDateTime = this.lastModifiedDate
  ) =
    SeriesMetadata(
      status = status,
      title = title,
      titleSort = titleSort,
      statusLock = statusLock,
      titleLock = titleLock,
      titleSortLock = titleSortLock,
      seriesId = seriesId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )

  enum class Status {
    ENDED, ONGOING, ABANDONED, HIATUS
  }

  override fun toString(): String =
    "SeriesMetadata(status=$status, statusLock=$statusLock, titleLock=$titleLock, titleSortLock=$titleSortLock, seriesId=$seriesId, createdDate=$createdDate, lastModifiedDate=$lastModifiedDate, title='$title', titleSort='$titleSort')"
}
