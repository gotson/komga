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
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable() {

  enum class Status {
    UNKNOWN, ERROR, READY, UNSUPPORTED, OUTDATED
  }
}
