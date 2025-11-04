package com.example.komga.plugin

import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.BookMetadataPatchCapability
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.infrastructure.plugin.KomgaPlugin
import org.gotson.komga.infrastructure.plugin.PluginBookMetadataProvider
import org.gotson.komga.infrastructure.plugin.PluginContext
import org.gotson.komga.infrastructure.plugin.PluginDescriptor
import org.gotson.komga.infrastructure.plugin.multiLanguage

/**
 * Example plugin demonstrating language-aware metadata fetching.
 *
 * This plugin shows how to:
 * - Fetch metadata in multiple languages from external sources
 * - Create BookMetadataPatch with multiple language titles
 * - Use the plugin builder API for language support
 */
class MangaDexLanguageAwarePlugin : PluginBookMetadataProvider {
  private lateinit var context: PluginContext

  override fun onLoad(context: PluginContext) {
    this.context = context
    context.log(PluginContext.LogLevel.INFO, "MangaDex Language-Aware Plugin loaded")

    // Initialize plugin configuration
    val apiKey = context.getConfig("api_key")
    if (apiKey == null) {
      context.log(PluginContext.LogLevel.WARN, "No API key configured")
    }
  }

  override fun onUnload() {
    context.log(PluginContext.LogLevel.INFO, "MangaDex Language-Aware Plugin unloaded")
  }

  override fun getDescriptor(): PluginDescriptor =
    PluginDescriptor(
      id = "mangadex-language-aware",
      name = "MangaDex Language-Aware Plugin",
      version = "1.0.0",
      description = "Fetches manga metadata from MangaDex with multi-language title support",
      author = "Komga Team",
      komgaVersion = "1.0.0",
      entryPoint = "com.example.komga.plugin.MangaDexLanguageAwarePlugin",
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

      // Extract manga title from filename
      val mangaTitle = extractMangaTitle(book.name)
      context.log(PluginContext.LogLevel.DEBUG, "Searching for manga: $mangaTitle")

      // Simulate fetching metadata from MangaDex in multiple languages
      val metadata = fetchMangaDexMetadata(mangaTitle)

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
   */
  private fun extractMangaTitle(filename: String): String {
    val nameWithoutExt = filename.substringBeforeLast(".")

    // Remove volume/chapter information
    val patterns = listOf(
      Regex("""\s+v\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+vol\.?\s*\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+ch\.?\s*\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+chapter\s+\d+.*$""", RegexOption.IGNORE_CASE),
      Regex("""\s+\(\d{4}\).*$"""),
      Regex("""\s+\[\d{4}\].*$"""),
    )

    var title = nameWithoutExt
    patterns.forEach { pattern ->
      title = pattern.replace(title, "")
    }

    return title.trim()
  }

  /**
   * Fetch manga metadata from MangaDex API with multiple language titles.
   *
   * In a real implementation, this would:
   * 1. Make HTTP request to MangaDex API
   * 2. Parse JSON response
   * 3. Extract titles in all available languages
   * 4. Create BookMetadataPatch with language map
   */
  private fun fetchMangaDexMetadata(title: String): BookMetadataPatch? {
    // Mock data for demonstration
    // In real implementation, this would fetch from MangaDex API

    // Example: Searching for "One Piece"
    if (title.contains("One Piece", ignoreCase = true)) {
      // Use the multi-language builder
      return BookMetadataPatch.multiLanguage
        .addTitle("en", "One Piece")
        .addTitle("ja", "ワンピース")
        .addTitle("ja-ro", "Wan Pīsu")
        .addTitle("ko", "원피스")
        .addTitle("zh-hans", "海贼王")
        .addTitle("zh-hant", "海賊王")
        .addTitle("fr", "One Piece")
        .addTitle("de", "One Piece")
        .addSummary("en", "The story follows Monkey D. Luffy, a young man who dreams of becoming the Pirate King...")
        .addSummary("ja", "海賊王を目指す少年モンキー・D・ルフィの物語...")
        .buildForBook()
        .copy(
          authors = listOf(
            Author("Oda, Eiichiro", "writer"),
            Author("Oda, Eiichiro", "penciller"),
          ),
        )
    }

    // Example: Searching for "Naruto"
    if (title.contains("Naruto", ignoreCase = true)) {
      return BookMetadataPatch.multiLanguage
        .addTitle("en", "Naruto")
        .addTitle("ja", "ナルト")
        .addTitle("ja-ro", "Naruto")
        .addTitle("ko", "나루토")
        .addTitle("zh-hans", "火影忍者")
        .addTitle("zh-hant", "火影忍者")
        .addSummary("en", "The story follows Naruto Uzumaki, a young ninja who seeks recognition...")
        .addSummary("ja", "落ちこぼれ忍者・うずまきナルトの成長物語...")
        .buildForBook()
        .copy(
          authors = listOf(
            Author("Kishimoto, Masashi", "writer"),
            Author("Kishimoto, Masashi", "penciller"),
          ),
        )
    }

    // Real implementation would make HTTP request here:
    // val httpClient = OkHttpClient()
    // val request = Request.Builder()
    //   .url("https://api.mangadex.org/manga?title=$title&includes[]=altTitles")
    //   .build()
    // val response = httpClient.newCall(request).execute()
    // val json = response.body?.string()
    // val manga = parseMangaDexResponse(json)
    //
    // return BookMetadataPatch.multiLanguage
    //   .addTitles(manga.allTitles) // Map of language -> title
    //   .addSummaries(manga.allDescriptions)
    //   .buildForBook()
    //   .copy(authors = manga.authors, ...)

    return null
  }
}

/**
 * Example showing different ways to create language-aware metadata patches.
 */
fun exampleUsage() {
  // Method 1: Using the builder API
  val patch1 = BookMetadataPatch.multiLanguage
    .addTitle("en", "Attack on Titan")
    .addTitle("ja", "進撃の巨人")
    .addTitle("ja-ro", "Shingeki no Kyojin")
    .buildForBook()

  // Method 2: Using the map directly
  val titles = mapOf(
    "en" to "My Hero Academia",
    "ja" to "僕のヒーローアカデミア",
    "ja-ro" to "Boku no Hero Academia",
  )
  val patch2 = BookMetadataPatch(
    title = "My Hero Academia", // Default/fallback
    titles = titles,
  )

  // Method 3: Adding languages to existing patch
  val patch3 = BookMetadataPatch(title = "Demon Slayer")
    .copy(
      titles = mapOf(
        "en" to "Demon Slayer",
        "ja" to "鬼滅の刃",
        "ja-ro" to "Kimetsu no Yaiba",
      ),
    )
}
