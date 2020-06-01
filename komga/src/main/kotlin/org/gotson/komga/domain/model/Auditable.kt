package org.gotson.komga.domain.model

import java.time.LocalDateTime

abstract class Auditable {
  abstract val createdDate: LocalDateTime
  abstract val lastModifiedDate: LocalDateTime
}
