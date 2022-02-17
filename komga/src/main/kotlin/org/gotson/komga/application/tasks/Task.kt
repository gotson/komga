package org.gotson.komga.application.tasks

import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.infrastructure.search.LuceneEntity
import java.io.Serializable

const val HIGHEST_PRIORITY = 8
const val HIGH_PRIORITY = 6
const val DEFAULT_PRIORITY = 4
const val LOW_PRIORITY = 2
const val LOWEST_PRIORITY = 0

sealed class Task(priority: Int = DEFAULT_PRIORITY, val groupId: String? = null) : Serializable {
  abstract fun uniqueId(): String
  val priority = priority.coerceIn(0, 9)

  class ScanLibrary(val libraryId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "SCAN_LIBRARY_$libraryId"
    override fun toString(): String = "ScanLibrary(libraryId='$libraryId', priority='$priority')"
  }

  class FindBooksToConvert(val libraryId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "FIND_BOOKS_TO_CONVERT_$libraryId"
    override fun toString(): String = "FindBooksToConvert(libraryId='$libraryId', priority='$priority')"
  }

  class FindBooksWithMissingPageHash(val libraryId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "FIND_BOOKS_WITH_MISSING_PAGE_HASH_$libraryId"
    override fun toString(): String = "FindBooksWithMissingPageHash(libraryId='$libraryId', priority='$priority')"
  }

  class FindDuplicatePagesToDelete(val libraryId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "FIND_DUPLICATE_PAGES_TO_DELETE_$libraryId"
    override fun toString(): String = "FindDuplicatePagesToDelete(libraryId='$libraryId', priority='$priority')"
  }

  class EmptyTrash(val libraryId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "EMPTY_TRASH_$libraryId"
    override fun toString(): String = "EmptyTrash(libraryId='$libraryId', priority='$priority')"
  }

  class AnalyzeBook(val bookId: String, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId() = "ANALYZE_BOOK_$bookId"
    override fun toString(): String = "AnalyzeBook(bookId='$bookId', priority='$priority')"
  }

  class GenerateBookThumbnail(val bookId: String, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId() = "GENERATE_BOOK_THUMBNAIL_$bookId"
    override fun toString(): String = "GenerateBookThumbnail(bookId='$bookId', priority='$priority')"
  }

  class RefreshBookMetadata(val bookId: String, val capabilities: Set<BookMetadataPatchCapability>, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId() = "REFRESH_BOOK_METADATA_$bookId"
    override fun toString(): String = "RefreshBookMetadata(bookId='$bookId', capabilities=$capabilities, priority='$priority')"
  }

  class HashBook(val bookId: String, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId() = "HASH_BOOK_$bookId"
    override fun toString(): String = "HashBook(bookId='$bookId', priority='$priority')"
  }

  class HashBookPages(val bookId: String, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId() = "HASH_BOOK_PAGES_$bookId"
    override fun toString(): String = "HashBookPages(bookId='$bookId', priority='$priority')"
  }

  class RefreshSeriesMetadata(val seriesId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority, seriesId.takeLast(1)) {
    override fun uniqueId() = "REFRESH_SERIES_METADATA_$seriesId"
    override fun toString(): String = "RefreshSeriesMetadata(seriesId='$seriesId', priority='$priority')"
  }

  class AggregateSeriesMetadata(val seriesId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority, seriesId.takeLast(1)) {
    override fun uniqueId() = "AGGREGATE_SERIES_METADATA_$seriesId"
    override fun toString(): String = "AggregateSeriesMetadata(seriesId='$seriesId', priority='$priority')"
  }

  class RefreshBookLocalArtwork(val bookId: String, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId(): String = "REFRESH_BOOK_LOCAL_ARTWORK_$bookId"
    override fun toString(): String = "RefreshBookLocalArtwork(bookId='$bookId', priority='$priority')"
  }

  class RefreshSeriesLocalArtwork(val seriesId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority, seriesId.takeLast(1)) {
    override fun uniqueId(): String = "REFRESH_SERIES_LOCAL_ARTWORK_$seriesId"
    override fun toString(): String = "RefreshSeriesLocalArtwork(seriesId=$seriesId, priority='$priority')"
  }

  class ImportBook(val sourceFile: String, val seriesId: String, val copyMode: CopyMode, val destinationName: String?, val upgradeBookId: String?, priority: Int = DEFAULT_PRIORITY) : Task(priority, seriesId.takeLast(1)) {
    override fun uniqueId(): String = "IMPORT_BOOK_${seriesId}_$sourceFile"
    override fun toString(): String =
      "ImportBook(sourceFile='$sourceFile', seriesId='$seriesId', copyMode=$copyMode, destinationName=$destinationName, upgradeBookId=$upgradeBookId, priority='$priority')"
  }

  class ConvertBook(val bookId: String, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId(): String = "CONVERT_BOOK_$bookId"
    override fun toString(): String = "ConvertBook(bookId='$bookId', priority='$priority')"
  }

  class RepairExtension(val bookId: String, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId(): String = "REPAIR_EXTENSION_$bookId"
    override fun toString(): String = "RepairExtension(bookId='$bookId', priority='$priority')"
  }

  class RemoveHashedPages(val bookId: String, val pages: Collection<BookPageNumbered>, priority: Int = DEFAULT_PRIORITY, groupId: String) : Task(priority, groupId.takeLast(1)) {
    override fun uniqueId(): String = "REMOVE_HASHED_PAGES_$bookId"
    override fun toString(): String = "RemoveHashedPages(bookId='$bookId', priority='$priority')"
  }

  class RebuildIndex(val entities: Set<LuceneEntity>?, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "REBUILD_INDEX"
    override fun toString(): String = "RebuildIndex(priority='$priority',entities='${entities?.map { it.type }}')"
  }

  class DeleteBook(val bookId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "DELETE_BOOK_$bookId"
    override fun toString(): String = "DeleteBook(bookId='$bookId', priority='$priority')"
  }

  class DeleteSeries(val seriesId: String, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "DELETE_SERIES_$seriesId"
    override fun toString(): String = "DeleteSeries(seriesId='$seriesId', priority='$priority')"
  }
}
