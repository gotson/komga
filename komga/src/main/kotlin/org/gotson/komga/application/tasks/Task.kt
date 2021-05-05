package org.gotson.komga.application.tasks

import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.CopyMode
import java.io.Serializable

const val HIGHEST_PRIORITY = 8
const val HIGH_PRIORITY = 6
const val DEFAULT_PRIORITY = 4
const val LOWEST_PRIORITY = 0

sealed class Task(priority: Int = DEFAULT_PRIORITY) : Serializable {
  abstract fun uniqueId(): String
  val priority = priority.coerceIn(0, 9)

  data class ScanLibrary(val libraryId: String) : Task() {
    override fun uniqueId() = "SCAN_LIBRARY_$libraryId"
  }

  class AnalyzeBook(val bookId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "ANALYZE_BOOK_$bookId"
    override fun toString(): String = "AnalyzeBook(bookId='$bookId', priority='$priority')"
  }

  class GenerateBookThumbnail(val bookId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "GENERATE_BOOK_THUMBNAIL_$bookId"
    override fun toString(): String = "GenerateBookThumbnail(bookId='$bookId', priority='$priority')"
  }

  class RefreshBookMetadata(val bookId: String, val capabilities: List<BookMetadataPatchCapability>, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "REFRESH_BOOK_METADATA_$bookId"
    override fun toString(): String = "RefreshBookMetadata(bookId='$bookId', capabilities=$capabilities, priority='$priority')"
  }

  data class RefreshSeriesMetadata(val seriesId: String) : Task() {
    override fun uniqueId() = "REFRESH_SERIES_METADATA_$seriesId"
  }

  data class AggregateSeriesMetadata(val seriesId: String) : Task() {
    override fun uniqueId() = "AGGREGATE_SERIES_METADATA_$seriesId"
  }

  class ImportBook(val sourceFile: String, val seriesId: String, val copyMode: CopyMode, val destinationName: String?, val upgradeBookId: String?, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId(): String = "IMPORT_BOOK_${seriesId}_$sourceFile"
    override fun toString(): String =
      "ImportBook(sourceFile='$sourceFile', seriesId='$seriesId', copyMode=$copyMode, destinationName=$destinationName, upgradeBookId=$upgradeBookId, priority='$priority')"
  }

  class ConvertBook(val bookId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId(): String = "CONVERT_BOOK_$bookId"
    override fun toString(): String = "ConvertBook(bookId='$bookId', priority='$priority')"
  }

  class RepairExtension(val bookId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId(): String = "REPAIR_EXTENSION_$bookId"
    override fun toString(): String = "RepairExtension(bookId='$bookId', priority='$priority')"
  }
}
