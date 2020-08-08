package org.gotson.komga.domain.model

import java.time.LocalDateTime

class Media(
  val status: Status = Status.UNKNOWN,
  val mediaType: String? = null,
  val thumbnail: ByteArray? = null,
  val pages: List<BookPage> = emptyList(),
  val files: List<String> = emptyList(),
  val comment: String? = null,
  val bookId: String = "",
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  fun copy(
    status: Status = this.status,
    mediaType: String? = this.mediaType,
    thumbnail: ByteArray? = this.thumbnail,
    pages: List<BookPage> = this.pages.toList(),
    files: List<String> = this.files.toList(),
    comment: String? = this.comment,
    bookId: String = this.bookId,
    createdDate: LocalDateTime = this.createdDate,
    lastModifiedDate: LocalDateTime = this.lastModifiedDate
  ) =
    Media(
      status = status,
      mediaType = mediaType,
      thumbnail = thumbnail,
      pages = pages,
      files = files,
      comment = comment,
      bookId = bookId,
      createdDate = createdDate,
      lastModifiedDate = lastModifiedDate
    )

  enum class Status {
    UNKNOWN, ERROR, READY, UNSUPPORTED, OUTDATED
  }

  override fun toString(): String =
    "Media(status=$status, mediaType=$mediaType, pages=$pages, files=$files, comment=$comment, bookId=$bookId, createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
}
