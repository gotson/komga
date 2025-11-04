# Manga Metadata Example Plugin

This is an example plugin demonstrating how to create a metadata provider plugin for Komga.

## Features

This example plugin shows how to:
- Implement the `PluginBookMetadataProvider` interface
- Extract manga titles from filenames
- Fetch metadata from external sources (structure provided)
- Handle errors gracefully
- Use the plugin context for logging and configuration

## Building

To build the plugin:

```bash
cd example-manga-plugin
./gradlew jar
```

The plugin JAR will be created in `build/libs/example-manga-plugin-1.0.0.jar`.

## Installing

1. Copy the JAR file to Komga's plugin directory (default: `~/.komga/plugins/`)
2. Restart Komga or use the "Reload Plugins" API endpoint
3. Enable the plugin for specific libraries via the Library settings

## Configuration

The plugin supports the following configuration options:
- `api_key`: API key for the manga metadata service

You can configure these via the Plugin API or in the Komga UI (when implemented).

## Real Implementation

To create a real manga metadata plugin, you would:

1. **Choose a manga database API**:
   - MangaDex API (https://api.mangadex.org/docs/)
   - AniList API (https://anilist.gitbook.io/anilist-apiv2-docs/)
   - MyAnimeList API (https://myanimelist.net/apiconfig/references/api/v2)

2. **Implement HTTP requests**:
   ```kotlin
   val client = OkHttpClient()
   val request = Request.Builder()
     .url("https://api.mangadex.org/manga?title=$title")
     .build()
   val response = client.newCall(request).execute()
   val json = response.body?.string()
   ```

3. **Parse JSON responses**:
   ```kotlin
   val mapper = jacksonObjectMapper()
   val data = mapper.readValue<MangaResponse>(json)
   ```

4. **Map to BookMetadataPatch**:
   ```kotlin
   return BookMetadataPatch(
     title = data.title,
     summary = data.description,
     authors = data.authors.map { Author(it.name, "writer") },
     releaseDate = LocalDate.parse(data.releaseDate)
   )
   ```

## Plugin API Reference

### Core Interfaces

- `KomgaPlugin`: Base interface for all plugins
- `PluginBookMetadataProvider`: For extracting book metadata
- `PluginSeriesMetadataProvider`: For extracting series metadata
- `PluginSeriesMetadataFromBookProvider`: For deriving series metadata from books

### Plugin Context

The `PluginContext` provides:
- `getDataDirectory()`: Get plugin's data directory for caching
- `getConfig(key)` / `setConfig(key, value)`: Get/set configuration
- `log(level, message)`: Log messages

### Capabilities

Declare which metadata fields your plugin can extract:
- `TITLE`: Book title
- `SUMMARY`: Book description
- `AUTHORS`: Author information
- `RELEASE_DATE`: Release date
- `NUMBER`: Book number in series
- `ISBN`: ISBN identifier
- `LINKS`: External links

## Example: MangaDex Integration

Here's a minimal example of fetching from MangaDex:

```kotlin
private fun fetchFromMangaDex(title: String): BookMetadataPatch? {
  val client = OkHttpClient()
  val encodedTitle = URLEncoder.encode(title, "UTF-8")
  val url = "https://api.mangadex.org/manga?title=$encodedTitle&limit=1"

  val request = Request.Builder()
    .url(url)
    .build()

  val response = client.newCall(request).execute()
  if (!response.isSuccessful) return null

  val json = response.body?.string() ?: return null
  val data = jacksonObjectMapper().readValue<MangaDexResponse>(json)

  val manga = data.data.firstOrNull() ?: return null

  return BookMetadataPatch(
    title = manga.attributes.title["en"] ?: manga.attributes.title.values.first(),
    summary = manga.attributes.description["en"],
    authors = manga.relationships
      .filter { it.type == "author" }
      .map { Author(it.attributes?.name ?: "", "writer") }
  )
}
```

## License

This example is provided as-is for educational purposes.
