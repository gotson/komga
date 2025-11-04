package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.infrastructure.plugin.ChapterCheckResult
import org.gotson.komga.infrastructure.plugin.PluginChapterChecker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Service for checking new chapters across all series using plugins.
 */
@Service
class ChapterCheckService(
  private val pluginLifecycle: PluginLifecycle,
  private val seriesRepository: SeriesRepository,
) {
  private val checkResults = ConcurrentHashMap<String, ChapterCheckResult>()

  /**
   * Check for new chapters for a specific series.
   * @param seriesId The series ID to check
   * @return List of check results from all applicable plugins
   */
  fun checkNewChapters(seriesId: String): List<ChapterCheckResult> {
    val series = seriesRepository.findByIdOrNull(seriesId) ?: return emptyList()

    return checkNewChaptersForSeries(series)
  }

  /**
   * Check for new chapters across all series.
   * This method can be scheduled to run periodically.
   */
  fun checkAllSeries(): Map<String, List<ChapterCheckResult>> {
    logger.info { "Checking for new chapters across all series" }

    val results = mutableMapOf<String, List<ChapterCheckResult>>()
    val allSeries = seriesRepository.findAll()

    allSeries.forEach { series ->
      val seriesResults = checkNewChaptersForSeries(series)
      if (seriesResults.isNotEmpty()) {
        results[series.id] = seriesResults
      }
    }

    logger.info { "Completed chapter check for ${allSeries.size} series, found updates in ${results.size} series" }

    return results
  }

  /**
   * Get cached check results.
   */
  fun getCachedResults(): Map<String, ChapterCheckResult> = checkResults.toMap()

  /**
   * Get cached result for a specific series.
   */
  fun getCachedResult(seriesId: String): ChapterCheckResult? = checkResults[seriesId]

  /**
   * Clear cached results.
   */
  fun clearCache() {
    checkResults.clear()
  }

  /**
   * Check for new chapters for a single series using all available plugins.
   */
  private fun checkNewChaptersForSeries(series: Series): List<ChapterCheckResult> {
    val results = mutableListOf<ChapterCheckResult>()

    // Get all loaded plugins that support chapter checking
    pluginLifecycle.getLoadedPlugins().forEach { plugin ->
      val loadedPlugin = pluginLifecycle.getLoadedPlugin(plugin.id)
      if (loadedPlugin?.plugin is PluginChapterChecker) {
        try {
          val checker = loadedPlugin.plugin as PluginChapterChecker
          val result = checker.checkNewChapters(series)

          if (result != null) {
            results.add(result)

            // Cache the result
            checkResults[series.id] = result

            if (result.hasNewChapters) {
              logger.info {
                "New chapters found for ${series.name}: ${result.newChapterCount} new chapters from ${result.source}"
              }
            }
          }
        } catch (e: Exception) {
          logger.error(e) { "Error checking new chapters for ${series.name} using plugin ${plugin.id}" }
        }
      }
    }

    return results
  }
}
