package org.gotson.komga.application.tasks

import java.io.Serializable

sealed class Task : Serializable {
  abstract fun uniqueId(): String

  data class ScanLibrary(val libraryId: Long) : Task() {
    override fun uniqueId() = "SCAN_LIBRARY_$libraryId"
  }

  data class AnalyzeBook(val bookId: Long) : Task() {
    override fun uniqueId() = "ANALYZE_BOOK_$bookId"
  }

  data class GenerateBookThumbnail(val bookId: Long) : Task() {
    override fun uniqueId() = "GENERATE_BOOK_THUMBNAIL_$bookId"
  }

  data class RefreshBookMetadata(val bookId: Long) : Task() {
    override fun uniqueId() = "REFRESH_BOOK_METADATA_$bookId"
  }

  data class RefreshSeriesMetadata(val seriesId: Long) : Task() {
    override fun uniqueId() = "REFRESH_SERIES_METADATA_$seriesId"
  }

  object BackupDatabase : Task() {
    override fun uniqueId(): String = "BACKUP_DATABASE"
    override fun toString(): String = "BackupDatabase"
  }
}
