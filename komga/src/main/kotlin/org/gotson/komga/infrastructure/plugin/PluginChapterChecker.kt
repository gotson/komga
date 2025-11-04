package org.gotson.komga.infrastructure.plugin

import org.gotson.komga.domain.model.Series

/**
 * Plugin interface for checking new chapters/updates.
 * Plugins implementing this interface can check external sources for new chapters.
 */
interface PluginChapterChecker : KomgaPlugin {
  /**
   * Check if new chapters are available for a series.
   * @param series The series to check
   * @return ChapterCheckResult containing information about new chapters
   */
  fun checkNewChapters(series: Series): ChapterCheckResult?

  /**
   * Get the update frequency for this checker (in minutes).
   * Used to determine how often to check for new chapters.
   */
  fun getUpdateFrequencyMinutes(): Int = 60 // Default: check every hour
}

/**
 * Result of checking for new chapters.
 */
data class ChapterCheckResult(
  val seriesId: String,
  val seriesTitle: String,
  val hasNewChapters: Boolean,
  val latestChapterNumber: String?,
  val latestChapterTitle: String?,
  val latestChapterUrl: String?,
  val newChapterCount: Int = 0,
  val lastChecked: Long = System.currentTimeMillis(),
  val source: String,
  val message: String? = null,
)

/**
 * Interface for plugins that can download chapters.
 */
interface PluginChapterDownloader : KomgaPlugin {
  /**
   * Download a chapter from an external source.
   * @param chapterUrl The URL or identifier of the chapter to download
   * @param destinationPath The destination directory to download to
   * @return ChapterDownloadResult containing download status
   */
  fun downloadChapter(chapterUrl: String, destinationPath: String): ChapterDownloadResult

  /**
   * Search for a series on the external source.
   * @param seriesTitle The title to search for
   * @return List of search results
   */
  fun searchSeries(seriesTitle: String): List<SeriesSearchResult>
}

/**
 * Result of a chapter download operation.
 */
data class ChapterDownloadResult(
  val success: Boolean,
  val filePath: String?,
  val chapterNumber: String?,
  val chapterTitle: String?,
  val error: String?,
)

/**
 * Result of a series search.
 */
data class SeriesSearchResult(
  val title: String,
  val alternativeTitles: List<String> = emptyList(),
  val url: String,
  val coverUrl: String?,
  val description: String?,
  val latestChapter: String?,
  val source: String,
  val authors: List<String> = emptyList(),
  val genres: List<String> = emptyList(),
)
