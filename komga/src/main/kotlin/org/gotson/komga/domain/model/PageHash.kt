package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class PageHash(
  val hash: String,
  val mediaType: String,
  val size: Long? = null,
  val action: Action,
  val deleteCount: Int = 0,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable() {
  enum class Action {
    DELETE_AUTO,
    DELETE_MANUAL,
    IGNORE,
  }
}
