package org.gotson.komga.domain.model

import java.time.LocalDateTime

class PageHashKnown(
  hash: String,
  mediaType: String,
  size: Long? = null,
  val action: Action,
  val deleteCount: Int = 0,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable, PageHash(hash, mediaType, size) {
  enum class Action {
    DELETE_AUTO,
    DELETE_MANUAL,
    IGNORE,
  }
}
