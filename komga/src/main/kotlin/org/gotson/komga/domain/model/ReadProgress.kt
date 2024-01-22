package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class ReadProgress(
  val bookId: String,
  val userId: String,
  val page: Int,
  val completed: Boolean,
  val readDate: LocalDateTime = LocalDateTime.now(),
  val deviceId: String = "",
  val deviceName: String = "",
  val locator: R2Locator? = null,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable
