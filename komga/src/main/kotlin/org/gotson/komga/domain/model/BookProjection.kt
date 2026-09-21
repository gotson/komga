package org.gotson.komga.domain.model

import java.time.LocalDateTime

/**
 * A representation of a book file converted to a different [profile] will have a different [fileSize].
 */
data class BookProjection(
  val bookId: String,
  val profile: String,
  val fileSize: Long,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable
