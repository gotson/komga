package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class SyncPoint(
  val id: String,
  val userId: String,
  val createdDate: LocalDateTime,
) {
  data class Book(
    val syncPointId: String,
    val bookId: String,
    val fileLastModified: LocalDateTime,
    val fileSize: Long,
    val fileHash: String,
    val metadataLastModifiedDate: LocalDateTime,
  )
}
