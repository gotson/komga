package org.gotson.komga.domain.model

import java.time.LocalDateTime

interface Auditable {
  val createdDate: LocalDateTime
  val lastModifiedDate: LocalDateTime
}
