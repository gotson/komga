package org.gotson.komga.application.tasks

import java.io.Serializable

sealed class Task : Serializable {
  abstract fun uniqueId(): String

  data class ScanLibrary(val libraryId: String) : Task() {
    override fun uniqueId() = "SCAN_LIBRARY_$libraryId"
  }

  data class AnalyzeBook(val bookId: String) : Task() {
    override fun uniqueId() = "ANALYZE_BOOK_$bookId"
  }

  data class GenerateBookThumbnail(val bookId: String) : Task() {
    override fun uniqueId() = "GENERATE_BOOK_THUMBNAIL_$bookId"
  }

  data class RefreshBookMetadata(val bookId: String) : Task() {
    override fun uniqueId() = "REFRESH_BOOK_METADATA_$bookId"
  }

  data class RefreshSeriesMetadata(val seriesId: String) : Task() {
    override fun uniqueId() = "REFRESH_SERIES_METADATA_$seriesId"
  }
}
