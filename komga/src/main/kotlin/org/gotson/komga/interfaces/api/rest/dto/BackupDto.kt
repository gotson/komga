package org.gotson.komga.interfaces.api.rest.dto

import java.time.LocalDateTime

data class BackupResultDto(
  val success: Boolean,
  val backupPath: String? = null,
  val fileName: String? = null,
  val sizeBytes: Long? = null,
  val sizeMB: Long? = null,
  val timestamp: LocalDateTime,
  val error: String? = null,
)

data class BackupInfoDto(
  val fileName: String,
  val sizeBytes: Long,
  val sizeMB: Long,
  val created: LocalDateTime,
)

data class BackupRequestDto(
  val includeImages: Boolean = false,
)

data class CleanupRequestDto(
  val keepCount: Int = 10,
)
