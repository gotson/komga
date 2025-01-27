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
    val thumbnailId: String?,
    val synced: Boolean,
  )

  data class ReadList(
    val syncPointId: String,
    val readListId: String,
    val readListName: String,
    val createdDate: ZonedDateTime,
    val lastModifiedDate: ZonedDateTime,
    val synced: Boolean,
  ) {
    companion object {
      const val ON_DECK_ID = "KOMGA-ONDECK"
    }

    data class Book(
      val syncPointId: String,
      val readListId: String,
      val bookId: String,
    )
  }
}
