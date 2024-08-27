package org.gotson.komga.domain.model

import java.time.ZonedDateTime

data class SyncPoint(
  val id: String,
  val userId: String,
  val apiKeyId: String?,
  val createdDate: ZonedDateTime,
) {
  data class Book(
    val syncPointId: String,
    val bookId: String,
    val createdDate: ZonedDateTime,
    val lastModifiedDate: ZonedDateTime,
    val fileLastModified: ZonedDateTime,
    val fileSize: Long,
    val fileHash: String,
    val metadataLastModifiedDate: ZonedDateTime,
    val synced: Boolean,
  )
}
