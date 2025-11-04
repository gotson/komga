package com.example.komga.plugin

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.infrastructure.plugin.KomgaPlugin
import org.gotson.komga.infrastructure.plugin.PluginBookMetadataProvider
import org.gotson.komga.infrastructure.plugin.PluginContext
import org.gotson.komga.infrastructure.plugin.PluginDescriptor
import java.time.LocalDate

/**
 * Example Manga Metadata Plugin
 *
 * This plugin demonstrates how to create a metadata provider that fetches
 * manga information from external sources. In a real implementation, this
 * would make HTTP requests to manga databases like MangaDex, AniList, etc.
 */
class MangaMetadataPlugin : PluginBookMetadataProvider {
  private lateinit var context: PluginContext

  override fun onLoad(context: PluginContext) {
    this.context = context
    context.log(PluginContext.LogLevel.INFO, "Manga Metadata Plugin loaded")

    // Initialize plugin (e.g., load API keys from config)
    val apiKey = context.getConfig("api_key")
    if (apiKey == null) {
      context.log(PluginContext.LogLevel.WARN, "No API key configured")
    }
  }

  override fun onUnload() {
    context.log(PluginContext.LogLevel.INFO, "Manga Metadata Plugin unloaded")
  }

  override fun getDescriptor(): PluginDescriptor =
    PluginDescriptor(
      id = "manga-metadata-example",
      name = "Manga Metadata Example Plugin",
      version = "1.0.0",
      description = "Example plugin that demonstrates fetching manga metadata from external sources",
      author = "Komga Team",
      komgaVersion = "1.0.0",
      entryPoint = "com.example.komga.plugin.MangaMetadataPlugin",
      type = "book-metadata",
      capabilities = listOf("TITLE", "SUMMARY", "AUTHORS", "RELEASE_DATE"),
    )

  override fun getCapabilities(): Set<BookMetadataPatchCapability> =
    setOf(
      BookMetadataPatchCapability.TITLE,
      BookMetadataPatchCapability.SUMMARY,
      BookMetadataPatchCapability.AUTHORS,
      BookMetadataPatchCapability.RELEASE_DATE,
    )

  override fun getBookMetadata(book: BookWithMedia): BookMetadataPatch? {
    try {
      context.log(PluginContext.LogLevel.DEBUG, "Extracting metadata for book: ${book.name}")

      // In a real implementation, you would:
      // 1. Extract manga title/ISBN from the book name or metadata
      // 2. Make HTTP requests to manga databases (MangaDex, AniList, MyAnimeList, etc.)
      // 3. Parse the response and create a BookMetadataPatch

      // Example: Search for manga by title
      val mangaTitle = extractMangaTitle(book.name)
      context.log(PluginContext.LogLevel.DEBUG, "Searching for manga: $mangaTitle")

      // Simulate fetching metadata from an external source
      val metadata = fetchMangaMetadata(mangaTitle)

      if (metadata != null) {
        context.log(PluginContext.LogLevel.INFO, "Found metadata for: $mangaTitle")
        return metadata
      }

      context.log(PluginContext.LogLevel.DEBUG, "No metadata found for: $mangaTitle")
      return null
    } catch (e: Exception) {
      context.log(PluginContext.LogLevel.ERROR, "Error extracting metadata: ${e.message}")
      return null
    }
  }

  /**
   * Extract manga title from book filename.
   * Example: "One Piece v01 (2020).cbz" -> "One Piece"
   */
  private fun extractMangaTitle(filename: String): String {
    // Remove file extension
    val nameWithoutExt = filename.substringBeforeLast(".")

    // Remove volume information (e.g., "v01", "Vol. 1", "Chapter 001")
    val patterns = listOf(
      Regex("""\s+v\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+vol\.?\s*\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+ch\.?\s*\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+chapter\s+\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+\(\d{4}\).*$"""), // Remove year in parentheses
      Regex("""\s+\[\d{4}\].*$"""), // Remove year in brackets
    )

    var title = nameWithoutExt
    patterns.forEach { pattern ->
      title = pattern.replace(title, "")
    }

    return title.trim()
  }

  /**
   * Fetch manga metadata from an external source.
   * In a real implementation, this would make HTTP requests.
   */
  private fun fetchMangaMetadata(title: String): BookMetadataPatch? {
    // This is a mock implementation for demonstration purposes
    // In a real plugin, you would:
    // 1. Make HTTP request to manga API (e.g., https://api.mangadex.org/manga)
    // 2. Parse JSON response
    // 3. Extract metadata fields
    // 4. Return BookMetadataPatch

    // Example implementation:
    // val httpClient = HttpClient.newHttpClient()
    // val request = HttpRequest.newBuilder()
    //   .uri(URI.create("https://api.mangadex.org/manga?title=$title"))
    //   .header("Content-Type", "application/json")
    //   .GET()
    //   .build()
    // val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    // val json = JSONObject(response.body())
    // ... parse and extract metadata

    // For this example, we'll return null (no metadata found)
    // Remove this in a real implementation
    return null
  }
}
