# Komga Plugin System

The Komga Plugin System allows developers to extend Komga's metadata import functionality by creating custom plugins. Plugins can fetch metadata from external sources like MangaDex, AniList, MyAnimeList, or any other manga/comic database.

## Overview

The plugin system provides:
- **Dynamic plugin loading** from JAR files
- **Multiple plugin types** for different metadata sources
- **Per-library configuration** to enable/disable plugins
- **REST API** for plugin management
- **Safe isolation** with separate classloaders
- **Lifecycle management** with initialization and cleanup hooks

## Architecture

### Plugin Types

Komga supports three types of metadata plugins:

1. **Book Metadata Provider** (`PluginBookMetadataProvider`)
   - Extracts metadata from individual books
   - Examples: ISBN lookup, barcode scanning, external manga databases

2. **Series Metadata Provider** (`PluginSeriesMetadataProvider`)
   - Extracts series-level metadata
   - Examples: Series databases, sidecar files

3. **Series-from-Book Metadata Provider** (`PluginSeriesMetadataFromBookProvider`)
   - Derives series metadata from individual books
   - Examples: Aggregating book metadata into series info

### Core Interfaces

All plugins must implement the `KomgaPlugin` interface:

```kotlin
interface KomgaPlugin {
  fun onLoad(context: PluginContext)
  fun onUnload()
  fun getDescriptor(): PluginDescriptor
}
```

Then implement one of the specialized provider interfaces:

```kotlin
interface PluginBookMetadataProvider : KomgaPlugin {
  fun getBookMetadata(book: BookWithMedia): BookMetadataPatch?
  fun getCapabilities(): Set<BookMetadataPatchCapability>
}

interface PluginSeriesMetadataProvider : KomgaPlugin {
  fun getSeriesMetadata(series: Series): SeriesMetadataPatch?
}

interface PluginSeriesMetadataFromBookProvider : KomgaPlugin {
  fun getSeriesMetadataFromBook(
    book: BookWithMedia,
    appendVolumeToTitle: Boolean
  ): SeriesMetadataPatch?
  fun supportsAppendVolume(): Boolean = false
}
```

## Creating a Plugin

### 1. Project Setup

Create a new Kotlin project with the following structure:

```
my-plugin/
├── komga-plugin.json          # Plugin descriptor
├── build.gradle.kts           # Build configuration
└── src/
    └── main/
        └── kotlin/
            └── MyPlugin.kt    # Plugin implementation
```

### 2. Plugin Descriptor

Create `komga-plugin.json` with your plugin's metadata:

```json
{
  "id": "my-manga-plugin",
  "name": "My Manga Plugin",
  "version": "1.0.0",
  "description": "Fetches manga metadata from XYZ database",
  "author": "Your Name",
  "komgaVersion": "1.0.0",
  "entryPoint": "com.example.MyPlugin",
  "type": "book-metadata",
  "capabilities": ["TITLE", "SUMMARY", "AUTHORS", "RELEASE_DATE"]
}
```

**Fields:**
- `id`: Unique plugin identifier (kebab-case)
- `name`: Human-readable plugin name
- `version`: Semantic version
- `description`: Brief description of functionality
- `author`: Plugin author
- `komgaVersion`: Minimum compatible Komga version
- `entryPoint`: Fully qualified class name of plugin entry point
- `type`: Plugin type (`book-metadata`, `series-metadata`, `series-from-book-metadata`)
- `capabilities`: List of metadata fields the plugin can extract

### 3. Plugin Implementation

```kotlin
package com.example

import org.gotson.komga.domain.model.*
import org.gotson.komga.infrastructure.plugin.*

class MyPlugin : PluginBookMetadataProvider {
  private lateinit var context: PluginContext

  override fun onLoad(context: PluginContext) {
    this.context = context
    context.log(PluginContext.LogLevel.INFO, "Plugin loaded")

    // Load configuration
    val apiKey = context.getConfig("api_key")
    // Initialize HTTP clients, caches, etc.
  }

  override fun onUnload() {
    context.log(PluginContext.LogLevel.INFO, "Plugin unloaded")
    // Cleanup resources
  }

  override fun getDescriptor(): PluginDescriptor {
    return PluginDescriptor(
      id = "my-manga-plugin",
      name = "My Manga Plugin",
      version = "1.0.0",
      description = "Fetches manga metadata from XYZ database",
      author = "Your Name",
      komgaVersion = "1.0.0",
      entryPoint = "com.example.MyPlugin",
      type = "book-metadata",
      capabilities = listOf("TITLE", "SUMMARY", "AUTHORS", "RELEASE_DATE")
    )
  }

  override fun getCapabilities(): Set<BookMetadataPatchCapability> {
    return setOf(
      BookMetadataPatchCapability.TITLE,
      BookMetadataPatchCapability.SUMMARY,
      BookMetadataPatchCapability.AUTHORS,
      BookMetadataPatchCapability.RELEASE_DATE
    )
  }

  override fun getBookMetadata(book: BookWithMedia): BookMetadataPatch? {
    try {
      // Extract identifier from book
      val title = extractTitle(book.name)

      // Fetch from external API
      val metadata = fetchFromAPI(title)

      // Return metadata patch
      return BookMetadataPatch(
        title = metadata.title,
        summary = metadata.description,
        authors = metadata.authors.map { Author(it, "writer") },
        releaseDate = metadata.releaseDate
      )
    } catch (e: Exception) {
      context.log(PluginContext.LogLevel.ERROR, "Error: ${e.message}")
      return null
    }
  }

  private fun extractTitle(filename: String): String {
    // Parse filename to extract manga title
    return filename.substringBeforeLast(".")
  }

  private fun fetchFromAPI(title: String): Metadata {
    // Make HTTP request to external API
    // Parse response
    // Return metadata
  }
}
```

### 4. Build Configuration

Create `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Komga plugin API (provided at runtime)
    compileOnly(fileTree(mapOf("dir" to "../komga/build/libs", "include" to listOf("*.jar"))))

    // Your dependencies
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    jar {
        // Include plugin descriptor
        from("komga-plugin.json") {
            into("")
        }

        // Create fat JAR with dependencies
        from(configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
```

### 5. Build the Plugin

```bash
./gradlew jar
```

The plugin JAR will be in `build/libs/my-plugin-1.0.0.jar`.

## Installing Plugins

### Method 1: Download from Built-in Repository

Komga includes built-in support for downloading plugins from curated repositories:

```bash
# List available plugin repositories
curl -X GET \
  -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/plugins/repositories

# Download and install manga-py plugin
curl -X POST \
  -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/plugins/download/manga-py
```

**Built-in Repositories:**
- `manga-py` - Python-based manga metadata fetcher from 08shiro80/manga-py

### Method 2: Download from URL

```bash
curl -X POST \
  -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/plugins/download-url?url=https://example.com/my-plugin.jar"
```

### Method 3: Manual Installation

1. Copy the plugin JAR to Komga's plugin directory:
   - Default: `~/.komga/plugins/`
   - Custom: Set via `komga.plugins.directory` property

2. Restart Komga or reload plugins via API

### Method 4: API Upload

```bash
curl -X POST \
  -H "Authorization: Bearer <token>" \
  -F "file=@my-plugin-1.0.0.jar" \
  http://localhost:8080/api/v1/plugins/upload
```

## REST API

### List Plugins

```
GET /api/v1/plugins
```

Response:
```json
[
  {
    "id": "my-manga-plugin",
    "name": "My Manga Plugin",
    "version": "1.0.0",
    "description": "Fetches manga metadata",
    "author": "Your Name",
    "enabled": true,
    "type": "BOOK_METADATA",
    "capabilities": ["TITLE", "SUMMARY", "AUTHORS"],
    "jarPath": "/home/user/.komga/plugins/my-plugin.jar",
    "loadedAt": "2024-01-15T10:30:00",
    "error": null
  }
]
```

### Get Plugin Details

```
GET /api/v1/plugins/{pluginId}
```

### List Plugin Repositories

```
GET /api/v1/plugins/repositories
```

Response:
```json
[
  {
    "id": "manga-py",
    "name": "Manga-Py Metadata Plugin",
    "owner": "08shiro80",
    "repo": "manga-py",
    "description": "Python-based manga metadata fetcher supporting multiple sources"
  }
]
```

### Download Plugin from Repository

```
POST /api/v1/plugins/download/{repoId}
```

Downloads and installs a plugin from a built-in repository.

Example:
```bash
curl -X POST \
  -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/plugins/download/manga-py
```

### Download Plugin from URL

```
POST /api/v1/plugins/download-url?url={url}&fileName={fileName}
```

Downloads and installs a plugin from a direct URL.

Parameters:
- `url` (required): Direct URL to the plugin JAR file
- `fileName` (optional): Custom filename for the downloaded plugin

Example:
```bash
curl -X POST \
  -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/plugins/download-url?url=https://github.com/user/plugin/releases/download/v1.0.0/plugin.jar"
```

### Reload All Plugins

```
POST /api/v1/plugins/reload
```

### Upload Plugin

```
POST /api/v1/plugins/upload
Content-Type: multipart/form-data

file: <plugin.jar>
```

### Unload Plugin

```
DELETE /api/v1/plugins/{pluginId}
```

### Plugin System Status

```
GET /api/v1/plugins/status
```

Response:
```json
{
  "loaded": 3,
  "total": 3,
  "pluginDirectory": "/home/user/.komga/plugins"
}
```

### Check for Plugin Updates

```
POST /api/v1/plugins/check-updates
```

Checks all loaded plugins for available updates from their repositories.

Response:
```json
[
  {
    "pluginId": "manga-py",
    "pluginName": "Manga-Py Metadata Plugin",
    "currentVersion": "1.0.0",
    "latestVersion": "1.1.0",
    "updateAvailable": true,
    "releaseUrl": "https://github.com/08shiro80/manga-py/releases/tag/v1.1.0",
    "releaseNotes": "- Added support for more sources\n- Fixed bugs",
    "error": null
  }
]
```

### Check for New Chapters

```
POST /api/v1/plugins/check-chapters/{seriesId}
```

Checks if new chapters are available for a specific series using loaded plugins.

Response:
```json
[
  {
    "seriesId": "123",
    "seriesTitle": "One Piece",
    "hasNewChapters": true,
    "latestChapterNumber": "1095",
    "latestChapterTitle": "A World in Shock",
    "latestChapterUrl": "https://mangadex.org/chapter/...",
    "newChapterCount": 3,
    "lastChecked": 1704297600000,
    "source": "MangaDex",
    "message": "3 new chapters available"
  }
]
```

### Check All Series for New Chapters

```
POST /api/v1/plugins/check-all-chapters
```

Checks all series in the library for new chapters.

Response:
```json
{
  "series-id-1": [
    {
      "seriesId": "123",
      "seriesTitle": "One Piece",
      "hasNewChapters": true,
      "newChapterCount": 3,
      ...
    }
  ],
  "series-id-2": [...]
}
```

## Configuration

### Application Properties

```properties
# Plugin directory
komga.plugins.directory=/path/to/plugins

# Plugin data directory (for caches, configs)
komga.plugins.data-directory=/path/to/plugin-data
```

### Per-Library Configuration

Plugins can be enabled/disabled per library through the Library API:

```json
{
  "pluginConfigurations": {
    "my-manga-plugin": {
      "enabled": true,
      "config": {
        "api_key": "your-api-key",
        "cache_ttl": "3600"
      }
    }
  }
}
```

## Plugin Context

The `PluginContext` provides plugins with access to Komga services:

### Data Directory

```kotlin
val dataDir = context.getDataDirectory()
// Returns: /home/user/.komga/plugin-data/my-manga-plugin/
```

Use this for caching metadata, storing temporary files, etc.

### Configuration

```kotlin
// Get configuration value
val apiKey = context.getConfig("api_key")

// Set configuration value (persisted in Library settings)
context.setConfig("api_key", "new-value")
```

### Logging

```kotlin
context.log(PluginContext.LogLevel.DEBUG, "Debug message")
context.log(PluginContext.LogLevel.INFO, "Info message")
context.log(PluginContext.LogLevel.WARN, "Warning message")
context.log(PluginContext.LogLevel.ERROR, "Error message")
```

Logs appear in Komga's standard log output with `[plugin-id]` prefix.

## Metadata Capabilities

Plugins declare which metadata fields they can extract:

- `TITLE`: Book title
- `SUMMARY`: Book description/synopsis
- `NUMBER`: Book number in series
- `NUMBER_SORT`: Sort order for book number
- `RELEASE_DATE`: Publication date
- `AUTHORS`: Author/creator information
- `TAGS`: Genre tags
- `ISBN`: ISBN identifier
- `LINKS`: External links (e.g., to database page)

## Chapter Checking

Plugins can implement the `PluginChapterChecker` interface to check for new chapters:

```kotlin
interface PluginChapterChecker : KomgaPlugin {
  fun checkNewChapters(series: Series): ChapterCheckResult?
  fun getUpdateFrequencyMinutes(): Int = 60
}
```

Example implementation:

```kotlin
class MangaDexChapterChecker : PluginChapterChecker {
  override fun checkNewChapters(series: Series): ChapterCheckResult? {
    // Search for series on MangaDex
    val mangaId = findMangaId(series.name)
    if (mangaId == null) return null

    // Fetch latest chapters
    val chapters = fetchLatestChapters(mangaId)
    val localChapters = series.books.map { it.number }

    // Determine new chapters
    val newChapters = chapters.filter { it.number !in localChapters }

    return ChapterCheckResult(
      seriesId = series.id,
      seriesTitle = series.name,
      hasNewChapters = newChapters.isNotEmpty(),
      latestChapterNumber = chapters.firstOrNull()?.number,
      latestChapterTitle = chapters.firstOrNull()?.title,
      latestChapterUrl = chapters.firstOrNull()?.url,
      newChapterCount = newChapters.size,
      source = "MangaDex"
    )
  }
}
```

## Chapter Downloading

Plugins can implement the `PluginChapterDownloader` interface to download chapters:

```kotlin
interface PluginChapterDownloader : KomgaPlugin {
  fun downloadChapter(chapterUrl: String, destinationPath: String): ChapterDownloadResult
  fun searchSeries(seriesTitle: String): List<SeriesSearchResult>
}
```

Example:

```kotlin
class MangaDexDownloader : PluginChapterDownloader {
  override fun downloadChapter(chapterUrl: String, destinationPath: String): ChapterDownloadResult {
    // Download chapter pages
    val pages = fetchChapterPages(chapterUrl)

    // Create CBZ archive
    val cbzPath = createCbz(pages, destinationPath)

    return ChapterDownloadResult(
      success = true,
      filePath = cbzPath,
      chapterNumber = extractChapterNumber(chapterUrl),
      chapterTitle = extractChapterTitle(chapterUrl),
      error = null
    )
  }

  override fun searchSeries(seriesTitle: String): List<SeriesSearchResult> {
    val results = searchMangaDex(seriesTitle)
    return results.map { manga ->
      SeriesSearchResult(
        title = manga.title,
        url = manga.url,
        coverUrl = manga.coverUrl,
        description = manga.description,
        latestChapter = manga.latestChapter,
        source = "MangaDex"
      )
    }
  }
}
```

## Best Practices

### Error Handling

Always return `null` on errors instead of throwing exceptions:

```kotlin
override fun getBookMetadata(book: BookWithMedia): BookMetadataPatch? {
  try {
    // ... fetch metadata
  } catch (e: Exception) {
    context.log(PluginContext.LogLevel.ERROR, "Error: ${e.message}")
    return null  // Don't throw
  }
}
```

### Caching

Cache metadata to avoid repeated API calls:

```kotlin
private val cache = mutableMapOf<String, BookMetadataPatch>()

override fun getBookMetadata(book: BookWithMedia): BookMetadataPatch? {
  val cacheKey = book.id
  if (cache.containsKey(cacheKey)) {
    return cache[cacheKey]
  }

  val metadata = fetchFromAPI(book.name)
  cache[cacheKey] = metadata
  return metadata
}
```

### Rate Limiting

Respect API rate limits:

```kotlin
private var lastRequest = System.currentTimeMillis()
private val minDelay = 1000L  // 1 second between requests

private fun fetchFromAPI(title: String): Metadata {
  val now = System.currentTimeMillis()
  val elapsed = now - lastRequest
  if (elapsed < minDelay) {
    Thread.sleep(minDelay - elapsed)
  }
  lastRequest = System.currentTimeMillis()

  // Make API request
}
```

### Configuration Validation

Validate configuration on load:

```kotlin
override fun onLoad(context: PluginContext) {
  this.context = context

  val apiKey = context.getConfig("api_key")
  if (apiKey.isNullOrEmpty()) {
    context.log(
      PluginContext.LogLevel.WARN,
      "No API key configured. Please set 'api_key' in plugin configuration."
    )
  }
}
```

## Example Plugins

### MangaDex Plugin

See `example-manga-plugin/` for a complete example demonstrating:
- Extracting manga titles from filenames
- Fetching metadata from external APIs
- Error handling and logging
- Configuration management

### Building the Example

```bash
cd example-manga-plugin
./gradlew jar
cp build/libs/example-manga-plugin-1.0.0.jar ~/.komga/plugins/
```

## Troubleshooting

### Plugin Not Loading

1. Check Komga logs for error messages
2. Verify `komga-plugin.json` is in the JAR root
3. Ensure entry point class name matches descriptor
4. Check plugin JAR is in correct directory

### ClassNotFoundException

- Make sure all dependencies are included in the fat JAR
- Check `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` in build.gradle.kts

### Metadata Not Appearing

1. Enable plugin in Library settings
2. Check plugin is loaded: `GET /api/v1/plugins`
3. Verify plugin capabilities match requested metadata
4. Check Komga logs for plugin errors
5. Trigger metadata refresh for books/series

## Security Considerations

- Plugins run with full Komga permissions
- Only install trusted plugins from known sources
- Review plugin code before installation
- Plugins can access filesystem and network
- Use per-library configuration to limit plugin scope

## Contributing

To contribute to the plugin system:

1. Report issues on GitHub
2. Submit pull requests for improvements
3. Share your plugins with the community
4. Improve documentation

## Resources

- Example plugin: `example-manga-plugin/`
- Plugin API source: `komga/src/main/kotlin/org/gotson/komga/infrastructure/plugin/`
- REST API documentation: OpenAPI spec at `/v3/api-docs`

## License

The Komga plugin system is part of Komga and follows the same license.
