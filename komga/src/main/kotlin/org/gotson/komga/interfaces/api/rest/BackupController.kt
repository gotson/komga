package org.gotson.komga.interfaces.api.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.service.DatabaseBackupService
import org.gotson.komga.interfaces.api.rest.dto.BackupInfoDto
import org.gotson.komga.interfaces.api.rest.dto.BackupRequestDto
import org.gotson.komga.interfaces.api.rest.dto.BackupResultDto
import org.gotson.komga.interfaces.api.rest.dto.CleanupRequestDto
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * REST controller for database backup operations.
 * All endpoints require admin role.
 */
@RestController
@RequestMapping("api/v1/backup", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ADMIN')")
class BackupController(
  private val backupService: DatabaseBackupService,
) {
  /**
   * Create a new database backup.
   */
  @PostMapping("create")
  fun createBackup(
    @RequestBody(required = false) request: BackupRequestDto?,
  ): BackupResultDto {
    logger.info { "Creating database backup (includeImages=${request?.includeImages ?: false})" }
    val result = backupService.createBackup(request?.includeImages ?: false)

    return BackupResultDto(
      success = result.success,
      backupPath = result.backupPath,
      fileName = result.fileName,
      sizeBytes = result.sizeBytes,
      sizeMB = result.sizeBytes?.div(1024 * 1024),
      timestamp = result.timestamp,
      error = result.error,
    )
  }

  /**
   * List all available backups.
   */
  @GetMapping("list")
  fun listBackups(): List<BackupInfoDto> {
    logger.debug { "Listing available backups" }
    return backupService.listBackups().map { backup ->
      BackupInfoDto(
        fileName = backup.fileName,
        sizeBytes = backup.sizeBytes,
        sizeMB = backup.sizeBytes / (1024 * 1024),
        created = backup.created,
      )
    }
  }

  /**
   * Download a specific backup file.
   */
  @GetMapping("download/{fileName}")
  fun downloadBackup(
    @PathVariable fileName: String,
  ): ResponseEntity<Resource> {
    logger.info { "Downloading backup: $fileName" }

    // Security check - only allow downloading files that start with komga_backup_
    if (!fileName.startsWith("komga_backup_") || !fileName.endsWith(".zip")) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid backup file name")
    }

    val backups = backupService.listBackups()
    val backup = backups.find { it.fileName == fileName }
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Backup not found: $fileName")

    val file = File(backup.filePath)
    if (!file.exists()) {
      throw ResponseStatusException(HttpStatus.NOT_FOUND, "Backup file not found: $fileName")
    }

    val resource = FileSystemResource(file)

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
      .header(HttpHeaders.CONTENT_TYPE, "application/zip")
      .header(HttpHeaders.CONTENT_LENGTH, file.length().toString())
      .body(resource)
  }

  /**
   * Delete a specific backup file.
   */
  @DeleteMapping("{fileName}")
  fun deleteBackup(
    @PathVariable fileName: String,
  ): ResponseEntity<Map<String, Any>> {
    logger.info { "Deleting backup: $fileName" }

    // Security check
    if (!fileName.startsWith("komga_backup_") || !fileName.endsWith(".zip")) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid backup file name")
    }

    val deleted = backupService.deleteBackup(fileName)

    return if (deleted) {
      ResponseEntity.ok(mapOf("success" to true, "message" to "Backup deleted successfully"))
    } else {
      ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(mapOf("success" to false, "message" to "Backup not found or could not be deleted"))
    }
  }

  /**
   * Clean up old backups, keeping only the specified number of recent backups.
   */
  @PostMapping("cleanup")
  fun cleanupOldBackups(
    @RequestBody(required = false) request: CleanupRequestDto?,
  ): ResponseEntity<Map<String, Any>> {
    val keepCount = request?.keepCount ?: 10
    logger.info { "Cleaning up old backups, keeping $keepCount most recent" }

    val backupsBefore = backupService.listBackups().size
    backupService.cleanupOldBackups(keepCount)
    val backupsAfter = backupService.listBackups().size
    val deleted = backupsBefore - backupsAfter

    return ResponseEntity.ok(
      mapOf(
        "success" to true,
        "deletedCount" to deleted,
        "remainingCount" to backupsAfter,
        "message" to "Cleaned up $deleted old backups",
      ),
    )
  }

  /**
   * Get backup system status and configuration.
   */
  @GetMapping("status")
  fun getBackupStatus(): Map<String, Any> {
    val backups = backupService.listBackups()
    val totalSize = backups.sumOf { it.sizeBytes }

    return mapOf(
      "backupCount" to backups.size,
      "totalSizeBytes" to totalSize,
      "totalSizeMB" to totalSize / (1024 * 1024),
      "oldestBackup" to (backups.lastOrNull()?.created?.toString() ?: "N/A"),
      "newestBackup" to (backups.firstOrNull()?.created?.toString() ?: "N/A"),
    )
  }
}
