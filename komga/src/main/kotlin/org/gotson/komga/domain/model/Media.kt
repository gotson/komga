package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class Media(
  val status: Status = Status.UNKNOWN,
  val mediaType: String? = null,
  val pages: List<BookPage> = emptyList(),
  val files: List<String> = emptyList(),
  val comment: String? = null,
  val bookId: String = "",
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {

  enum class Status {
    UNKNOWN, ERROR, READY, UNSUPPORTED, OUTDATED
  }

  override fun toString(): String {
    return "Media(status=$status, mediaType=$mediaType, comment=$comment, bookId='$bookId', createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
  }
}
