package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val logger = KotlinLogging.logger {}

/**
 * Service for backing up and exporting the Komga database.
 */
@Service
class DatabaseBackupService(
  @Value("\${komga.database.file:#{systemProperties['user.home']}/.komga/database.sqlite}")
  private val databasePath: String,
  @Value("\${komga.backup.directory:#{systemProperties['user.home']}/.komga/backups}")
  private val backupDirectory: String,
) {
  /**
   * Create a backup of the database.
   */
  fun createBackup(includeImages: Boolean = false): BackupResult {
    return try {
      val backupDir = File(backupDirectory)
      backupDir.mkdirs()

      val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
      val backupFileName = "komga_backup_$timestamp.zip"
      val backupFile = File(backupDir, backupFileName)

      logger.info { "Creating database backup: ${backupFile.absolutePath}" }

      ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
        // Add database file
        val dbFile = File(databasePath)
        if (dbFile.exists()) {
          addFileToZip(zipOut, dbFile, "database.sqlite")
        }

        // Add database journal if exists
        val journalFile = File("$databasePath-journal")
        if (journalFile.exists()) {
          addFileToZip(zipOut, journalFile, "database.sqlite-journal")
        }

        // Add WAL file if exists
        val walFile = File("$databasePath-wal")
        if (walFile.exists()) {
          addFileToZip(zipOut, walFile, "database.sqlite-wal")
        }

        // Add SHM file if exists
        val shmFile = File("$databasePath-shm")
        if (shmFile.exists()) {
          addFileToZip(zipOut, shmFile, "database.sqlite-shm")
        }

        // Optionally include thumbnail images
        if (includeImages) {
          logger.info { "Including thumbnail images in backup" }
          val thumbnailDir = File(File(databasePath).parentFile, "thumbnails")
          if (thumbnailDir.exists() && thumbnailDir.isDirectory) {
            addDirectoryToZip(zipOut, thumbnailDir, "thumbnails")
          }
        }
      }

      val fileSize = backupFile.length()
      logger.info { "Backup created successfully: ${backupFile.name} (${fileSize / 1024 / 1024}MB)" }

      BackupResult(
        success = true,
        backupPath = backupFile.absolutePath,
        fileName = backupFileName,
        sizeBytes = fileSize,
        timestamp = LocalDateTime.now(),
      )
    } catch (e: Exception) {
      logger.error(e) { "Failed to create database backup" }
      BackupResult(
        success = false,
        error = "Backup failed: ${e.message}",
        timestamp = LocalDateTime.now(),
      )
    }
  }

  /**
   * Export database to a specific file.
   */
  fun exportDatabase(targetFile: File): BackupResult {
    return try {
      val dbFile = File(databasePath)
      if (!dbFile.exists()) {
        return BackupResult(
          success = false,
          error = "Database file not found: $databasePath",
          timestamp = LocalDateTime.now(),
        )
      }

      logger.info { "Exporting database to: ${targetFile.absolutePath}" }

      // Copy database file
      Files.copy(dbFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

      val fileSize = targetFile.length()
      logger.info { "Database exported successfully: ${targetFile.name} (${fileSize / 1024 / 1024}MB)" }

      BackupResult(
        success = true,
        backupPath = targetFile.absolutePath,
        fileName = targetFile.name,
        sizeBytes = fileSize,
        timestamp = LocalDateTime.now(),
      )
    } catch (e: Exception) {
      logger.error(e) { "Failed to export database" }
      BackupResult(
        success = false,
        error = "Export failed: ${e.message}",
        timestamp = LocalDateTime.now(),
      )
    }
  }

  /**
   * List available backups.
   */
  fun listBackups(): List<BackupInfo> {
    val backupDir = File(backupDirectory)
    if (!backupDir.exists() || !backupDir.isDirectory) {
      return emptyList()
    }

    return backupDir.listFiles { file ->
      file.isFile && file.name.startsWith("komga_backup_") && file.name.endsWith(".zip")
    }?.map { file ->
      BackupInfo(
        fileName = file.name,
        filePath = file.absolutePath,
        sizeBytes = file.length(),
        created = LocalDateTime.ofInstant(
          java.time.Instant.ofEpochMilli(file.lastModified()),
          java.time.ZoneId.systemDefault(),
        ),
      )
    }?.sortedByDescending { it.created } ?: emptyList()
  }

  /**
   * Delete a backup file.
   */
  fun deleteBackup(fileName: String): Boolean {
    return try {
      val backupFile = File(backupDirectory, fileName)
      if (backupFile.exists() && backupFile.name.startsWith("komga_backup_")) {
        backupFile.delete()
        logger.info { "Deleted backup: $fileName" }
        true
      } else {
        false
      }
    } catch (e: Exception) {
      logger.error(e) { "Failed to delete backup: $fileName" }
      false
    }
  }

  /**
   * Clean up old backups, keeping only the specified number of recent backups.
   */
  fun cleanupOldBackups(keepCount: Int = 10) {
    val backups = listBackups()
    if (backups.size <= keepCount) {
      return
    }

    val toDelete = backups.drop(keepCount)
    toDelete.forEach { backup ->
      deleteBackup(backup.fileName)
    }

    logger.info { "Cleaned up ${toDelete.size} old backups, keeping ${keepCount} most recent" }
  }

  /**
   * Add a file to zip archive.
   */
  private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryName: String) {
    file.inputStream().use { input ->
      zipOut.putNextEntry(ZipEntry(entryName))
      input.copyTo(zipOut)
      zipOut.closeEntry()
    }
  }

  /**
   * Add a directory recursively to zip archive.
   */
  private fun addDirectoryToZip(zipOut: ZipOutputStream, dir: File, basePath: String) {
    dir.listFiles()?.forEach { file ->
      val entryPath = "$basePath/${file.name}"
      if (file.isDirectory) {
        addDirectoryToZip(zipOut, file, entryPath)
      } else {
        addFileToZip(zipOut, file, entryPath)
      }
    }
  }

  data class BackupResult(
    val success: Boolean,
    val backupPath: String? = null,
    val fileName: String? = null,
    val sizeBytes: Long? = null,
    val timestamp: LocalDateTime,
    val error: String? = null,
  )

  data class BackupInfo(
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val created: LocalDateTime,
  )
}
