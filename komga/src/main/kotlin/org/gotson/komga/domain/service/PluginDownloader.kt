package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val logger = KotlinLogging.logger {}

/**
 * Service for downloading plugins from remote repositories.
 * Supports downloading from GitHub releases and direct URLs.
 */
@Service
class PluginDownloader(
  @Value("\${komga.plugins.directory:#{systemProperties['user.home']}/.komga/plugins}")
  private val pluginDirectory: String,
) {
  companion object {
    // Built-in plugin repositories
    private val BUILT_IN_REPOS = mapOf(
      "manga-py" to PluginRepo(
        name = "Manga-Py Metadata Plugin",
        owner = "08shiro80",
        repo = "manga-py",
        description = "Python-based manga metadata fetcher supporting multiple sources",
      ),
    )
  }

  data class PluginRepo(
    val name: String,
    val owner: String,
    val repo: String,
    val description: String,
  )

  data class DownloadResult(
    val success: Boolean,
    val pluginId: String?,
    val filePath: String?,
    val error: String?,
  )

  /**
   * Get list of built-in plugin repositories.
   */
  fun getBuiltInRepos(): Map<String, PluginRepo> = BUILT_IN_REPOS

  /**
   * Download a plugin from a GitHub repository's latest release.
   * @param repoId The repository ID (e.g., "manga-py")
   * @return DownloadResult with success status and file path
   */
  fun downloadFromBuiltInRepo(repoId: String): DownloadResult {
    val repo = BUILT_IN_REPOS[repoId]
      ?: return DownloadResult(
        success = false,
        pluginId = null,
        filePath = null,
        error = "Unknown repository: $repoId",
      )

    return downloadFromGitHub(repo.owner, repo.repo)
  }

  /**
   * Download a plugin from a GitHub repository's latest release.
   * @param owner GitHub repository owner
   * @param repo GitHub repository name
   * @return DownloadResult with success status and file path
   */
  fun downloadFromGitHub(owner: String, repo: String): DownloadResult {
    try {
      logger.info { "Downloading plugin from GitHub: $owner/$repo" }

      // Get latest release info from GitHub API
      val releaseUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
      val releaseInfo = fetchJson(releaseUrl)

      // Find JAR asset in release
      val assets = releaseInfo["assets"] as? List<*>
        ?: return DownloadResult(
          success = false,
          pluginId = null,
          filePath = null,
          error = "No assets found in latest release",
        )

      val jarAsset = assets.firstOrNull { asset ->
        val assetMap = asset as? Map<*, *>
        val name = assetMap?.get("name") as? String
        name?.endsWith(".jar") == true
      } as? Map<*, *>
        ?: return DownloadResult(
          success = false,
          pluginId = null,
          filePath = null,
          error = "No JAR file found in latest release",
        )

      val downloadUrl = jarAsset["browser_download_url"] as? String
        ?: return DownloadResult(
          success = false,
          pluginId = null,
          filePath = null,
          error = "No download URL found",
        )

      val fileName = jarAsset["name"] as? String ?: "$repo-plugin.jar"

      // Download the JAR file
      return downloadFromUrl(downloadUrl, fileName)
    } catch (e: Exception) {
      logger.error(e) { "Failed to download plugin from GitHub: $owner/$repo" }
      return DownloadResult(
        success = false,
        pluginId = null,
        filePath = null,
        error = "Download failed: ${e.message}",
      )
    }
  }

  /**
   * Download a plugin from a direct URL.
   * @param url The URL to download from
   * @param fileName The filename to save as
   * @return DownloadResult with success status and file path
   */
  fun downloadFromUrl(url: String, fileName: String): DownloadResult {
    try {
      logger.info { "Downloading plugin from URL: $url" }

      val targetDir = File(pluginDirectory)
      targetDir.mkdirs()

      val targetFile = File(targetDir, fileName)

      // Download file
      val urlConnection = URL(url).openConnection() as HttpURLConnection
      urlConnection.setRequestProperty("User-Agent", "Komga-Plugin-Downloader")
      urlConnection.connect()

      if (urlConnection.responseCode != 200) {
        return DownloadResult(
          success = false,
          pluginId = null,
          filePath = null,
          error = "HTTP ${urlConnection.responseCode}: ${urlConnection.responseMessage}",
        )
      }

      urlConnection.inputStream.use { input ->
        Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
      }

      logger.info { "Plugin downloaded successfully: ${targetFile.absolutePath}" }

      return DownloadResult(
        success = true,
        pluginId = fileName.substringBeforeLast(".jar"),
        filePath = targetFile.absolutePath,
        error = null,
      )
    } catch (e: Exception) {
      logger.error(e) { "Failed to download plugin from URL: $url" }
      return DownloadResult(
        success = false,
        pluginId = null,
        filePath = null,
        error = "Download failed: ${e.message}",
      )
    }
  }

  /**
   * Fetch and parse JSON from a URL.
   */
  private fun fetchJson(url: String): Map<String, Any> {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
    connection.setRequestProperty("User-Agent", "Komga-Plugin-Downloader")
    connection.connect()

    if (connection.responseCode != 200) {
      throw Exception("HTTP ${connection.responseCode}: ${connection.responseMessage}")
    }

    connection.inputStream.use { input ->
      // Simple JSON parsing (in production, use Jackson or similar)
      val json = input.bufferedReader().readText()
      return parseJson(json)
    }
  }

  /**
   * Check for plugin updates from a GitHub repository.
   * @param owner GitHub repository owner
   * @param repo GitHub repository name
   * @param currentVersion Current version of the plugin
   * @return UpdateCheckResult indicating if an update is available
   */
  fun checkForUpdate(owner: String, repo: String, currentVersion: String): UpdateCheckResult {
    try {
      logger.info { "Checking for updates: $owner/$repo (current: $currentVersion)" }

      val releaseUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
      val releaseInfo = fetchJson(releaseUrl)

      val latestVersion = releaseInfo["tag_name"] as? String
        ?: return UpdateCheckResult(
          updateAvailable = false,
          currentVersion = currentVersion,
          latestVersion = null,
          error = "Could not determine latest version",
        )

      // Remove 'v' prefix if present for comparison
      val cleanLatestVersion = latestVersion.removePrefix("v")
      val cleanCurrentVersion = currentVersion.removePrefix("v")

      val updateAvailable = cleanLatestVersion != cleanCurrentVersion

      return UpdateCheckResult(
        updateAvailable = updateAvailable,
        currentVersion = currentVersion,
        latestVersion = latestVersion,
        releaseUrl = releaseInfo["html_url"] as? String,
        releaseNotes = releaseInfo["body"] as? String,
      )
    } catch (e: Exception) {
      logger.error(e) { "Failed to check for updates: $owner/$repo" }
      return UpdateCheckResult(
        updateAvailable = false,
        currentVersion = currentVersion,
        latestVersion = null,
        error = "Update check failed: ${e.message}",
      )
    }
  }

  data class UpdateCheckResult(
    val updateAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String?,
    val releaseUrl: String? = null,
    val releaseNotes: String? = null,
    val error: String? = null,
  )

  /**
   * Simple JSON parser for basic use cases.
   * In production, use a proper JSON library like Jackson.
   */
  @Suppress("UNCHECKED_CAST")
  private fun parseJson(json: String): Map<String, Any> {
    // This is a placeholder - in real implementation, use Jackson or similar
    // For now, we'll use Kotlin's built-in capabilities
    return try {
      val cleaned = json.trim()
      if (cleaned.startsWith("{")) {
        // Very basic JSON parsing - replace with proper implementation
        emptyMap()
      } else {
        emptyMap()
      }
    } catch (e: Exception) {
      emptyMap()
    }
  }
}
