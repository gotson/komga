```markdown
# Language Priority System

Komga now supports multi-language title selection, allowing you to prefer specific languages when importing metadata from plugins and external sources.

## Overview

The language priority system enables:
- **Multiple language titles** for manga, comics, and series
- **Configurable language preferences** per library
- **Automatic title selection** based on your language priority
- **Romaji support** for Japanese and Korean titles
- **Fallback handling** when preferred languages aren't available

## Features

### 1. Multi-Language Title Support

Metadata providers can now supply titles in multiple languages, and Komga will automatically select the best match based on your preferences.

**Supported Languages:**
- English (en, en-us)
- Japanese (ja, ja-ro for romaji)
- Korean (ko, ko-ro for romaji)
- Chinese Simplified (zh-hans)
- Chinese Traditional (zh-hant)
- French (fr)
- German (de)
- Spanish (es)
- Italian (it)
- Portuguese (pt, pt-br)
- Russian (ru)
- Thai (th)
- Indonesian (id)
- Vietnamese (vi)

### 2. Library Language Settings

Configure language preferences per library:

```json
{
  "titleLanguagePriority": ["en", "en-us", "ja-ro", "ja"],
  "preferRomajiTitles": false,
  "fallbackToOriginalTitle": true
}
```

**Settings:**
- `titleLanguagePriority` - Ordered list of preferred languages (most preferred first)
- `preferRomajiTitles` - If true, prioritize romaji over native script
- `fallbackToOriginalTitle` - If true, use original title when no preferred language available

### 3. Automatic Language Detection

Komga can automatically detect the language of titles based on character sets:
- Japanese: Hiragana, Katakana, Kanji
- Korean: Hangul
- Chinese: CJK Unified Ideographs
- Thai: Thai script
- Russian: Cyrillic
- Default: English for ASCII text

## Library Configuration

### Setting Language Priority

**Via REST API:**

```bash
curl -X PATCH \
  -H "Content-Type: application/json" \
  -d '{
    "titleLanguagePriority": ["en", "ja-ro", "ja", "ko"],
    "preferRomajiTitles": false,
    "fallbackToOriginalTitle": true
  }' \
  http://localhost:8080/api/v1/libraries/{libraryId}
```

**Common Configurations:**

**English Preference:**
```json
{
  "titleLanguagePriority": ["en", "en-us", "ja-ro", "ja"],
  "preferRomajiTitles": false
}
```

**Romaji Preference:**
```json
{
  "titleLanguagePriority": ["ja-ro", "en", "ja"],
  "preferRomajiTitles": true
}
```

**Native Japanese Preference:**
```json
{
  "titleLanguagePriority": ["ja", "ja-ro", "en"],
  "preferRomajiTitles": false
}
```

**Multi-language Preference:**
```json
{
  "titleLanguagePriority": ["en", "fr", "de", "es", "ja-ro"],
  "preferRomajiTitles": false
}
```

## Plugin Development

### Creating Language-Aware Plugins

Plugins can provide metadata in multiple languages for automatic selection:

#### Method 1: Using the Builder API

```kotlin
class MyPlugin : PluginBookMetadataProvider {
  override fun getBookMetadata(book: BookWithMedia): BookMetadataPatch? {
    return BookMetadataPatch.multiLanguage
      .addTitle("en", "One Piece")
      .addTitle("ja", "ワンピース")
      .addTitle("ja-ro", "Wan Pīsu")
      .addTitle("ko", "원피스")
      .addTitle("zh-hans", "海贼王")
      .addSummary("en", "The story follows Monkey D. Luffy...")
      .addSummary("ja", "海賊王を目指す少年...")
      .buildForBook()
      .copy(
        authors = listOf(Author("Oda, Eiichiro", "writer"))
      )
  }
}
```

#### Method 2: Direct Map Creation

```kotlin
override fun getBookMetadata(book: BookWithMedia): BookMetadataPatch? {
  val titles = mapOf(
    "en" to "Attack on Titan",
    "ja" to "進撃の巨人",
    "ja-ro" to "Shingeki no Kyojin",
    "ko" to "진격의 거인"
  )

  return BookMetadataPatch(
    title = "Attack on Titan", // Default fallback
    titles = titles
  )
}
```

#### Method 3: Extending Existing Patches

```kotlin
override fun getBookMetadata(book: BookWithMedia): BookMetadataPatch? {
  val basePatch = BookMetadataPatch(title = "My Hero Academia")

  return basePatch.withLanguageTitles(
    mapOf(
      "en" to "My Hero Academia",
      "ja" to "僕のヒーローアカデミア",
      "ja-ro" to "Boku no Hero Academia"
    )
  )
}
```

### Real-World Example: MangaDex Integration

```kotlin
private fun fetchFromMangaDex(mangaId: String): BookMetadataPatch? {
  // Fetch manga data from MangaDex API
  val response = httpClient.get("https://api.mangadex.org/manga/$mangaId")
  val manga = json.decodeFromString<MangaDexManga>(response)

  // Extract titles in all available languages
  val allTitles = mutableMapOf<String, String>()

  // Main title
  manga.attributes.title.forEach { (lang, title) ->
    allTitles[lang] = title
  }

  // Alternative titles
  manga.attributes.altTitles.forEach { altTitle ->
    altTitle.forEach { (lang, title) ->
      allTitles[lang] = title
    }
  }

  // Extract descriptions
  val allDescriptions = manga.attributes.description

  // Create multi-language patch
  return BookMetadataPatch.multiLanguage
    .addTitles(allTitles)
    .addSummaries(allDescriptions)
    .buildForBook()
    .copy(
      authors = manga.authors.map { Author(it.name, "writer") },
      tags = manga.tags.toSet(),
      releaseDate = manga.releaseDate
    )
}
```

## API Reference

### Language Endpoints

#### List Supported Languages

```
GET /api/v1/languages
```

Response:
```json
[
  {
    "code": "en",
    "name": "English",
    "isRomaji": false
  },
  {
    "code": "ja",
    "name": "Japanese",
    "isRomaji": false
  },
  {
    "code": "ja-ro",
    "name": "Japanese (Romaji)",
    "isRomaji": true
  }
]
```

#### Get Default Priority

```
GET /api/v1/languages/default-priority
```

Response:
```json
["en", "en-us", "ja-ro", "ja"]
```

#### Test Title Selection

```
POST /api/v1/languages/select-title
```

Request:
```json
{
  "titles": {
    "en": "Demon Slayer",
    "ja": "鬼滅の刃",
    "ja-ro": "Kimetsu no Yaiba"
  },
  "languagePriority": ["ja-ro", "en", "ja"],
  "preferRomaji": true,
  "fallbackToOriginal": true
}
```

Response:
```json
{
  "selectedTitle": "Kimetsu no Yaiba",
  "selectedLanguage": "ja-ro"
}
```

#### Detect Language

```
POST /api/v1/languages/detect
```

Request:
```json
{
  "text": "進撃の巨人"
}
```

Response:
```json
{
  "text": "進撃の巨人",
  "detectedLanguage": "ja",
  "languageName": "Japanese"
}
```

#### Normalize Language Code

```
POST /api/v1/languages/normalize
```

Request:
```json
{
  "code": "ENG"
}
```

Response:
```json
{
  "original": "ENG",
  "normalized": "en",
  "languageName": "English"
}
```

## Title Selection Algorithm

The system selects titles using the following priority:

1. **Romaji Preference Check** (if enabled)
   - If `preferRomajiTitles` is true, look for romaji titles first

2. **Language Priority Matching**
   - Try exact match with each language in priority order
   - Try normalized match (e.g., "ENG" → "en")
   - Try base language match (e.g., "en" matches "en-us")

3. **Fallback**
   - Use original/default title if no matches found

**Example:**

Given titles:
```json
{
  "en": "One Piece",
  "ja": "ワンピース",
  "ja-ro": "Wan Pīsu",
  "ko": "원피스"
}
```

Priority: `["en", "ja-ro", "ja"]`

Result: `"One Piece"` (exact match on first priority)

Priority: `["ja-ro", "en", "ja"]`

Result: `"Wan Pīsu"` (exact match on first priority)

Priority: `["zh", "ko", "en"]`

Result: `"원피스"` (match on second priority, first not available)

## Use Cases

### Use Case 1: English-Preferred Manga Library

**Configuration:**
```json
{
  "titleLanguagePriority": ["en", "en-us", "ja-ro", "ja"],
  "preferRomajiTitles": false
}
```

**Result:**
- Displays English titles when available
- Falls back to romaji if no English title
- Uses native Japanese as last resort

**Example:**
- "Attack on Titan" (English available)
- "Kaguya-sama: Love is War" (English available)
- "Bocchi the Rock!" (English available)

### Use Case 2: Romaji-Preferred Library

**Configuration:**
```json
{
  "titleLanguagePriority": ["ja-ro", "en", "ja"],
  "preferRomajiTitles": true
}
```

**Result:**
- Prioritizes romaji titles
- Falls back to English
- Uses native Japanese last

**Example:**
- "Kimetsu no Yaiba" (romaji preferred)
- "Shingeki no Kyojin" (romaji preferred)
- "Boku no Hero Academia" (romaji preferred)

### Use Case 3: Native Language Library

**Configuration:**
```json
{
  "titleLanguagePriority": ["ja", "en"],
  "preferRomajiTitles": false
}
```

**Result:**
- Shows native Japanese titles
- Falls back to English

**Example:**
- "ワンピース" (native Japanese)
- "進撃の巨人" (native Japanese)
- "僕のヒーローアカデミア" (native Japanese)

### Use Case 4: Multi-Language European Library

**Configuration:**
```json
{
  "titleLanguagePriority": ["fr", "de", "es", "en"],
  "preferRomajiTitles": false
}
```

**Result:**
- Tries French first
- Then German
- Then Spanish
- Falls back to English

## Language Detection

### Automatic Detection

Komga can detect languages based on character sets:

```kotlin
languageService.detectLanguage("ワンピース")  // Returns: "ja"
languageService.detectLanguage("Wan Pīsu")     // Returns: "ja-ro"
languageService.detectLanguage("One Piece")    // Returns: "en"
languageService.detectLanguage("원피스")       // Returns: "ko"
languageService.detectLanguage("海贼王")       // Returns: "zh"
```

### Supported Detection

| Script | Languages | Detection |
|--------|-----------|-----------|
| Hiragana/Katakana/Kanji | Japanese | Automatic |
| ASCII-heavy with Japanese | Romaji | Heuristic |
| Hangul | Korean | Automatic |
| CJK Ideographs | Chinese | Automatic |
| Thai | Thai | Automatic |
| Cyrillic | Russian | Automatic |
| ASCII | English | Default |

## Best Practices

### For Plugin Developers

1. **Provide Multiple Languages**
   - Include at least English and native language
   - Add romaji for CJK languages
   - Include alternate titles

2. **Use Standard Language Codes**
   - Follow ISO 639-1 (two-letter codes)
   - Use extensions for variants (e.g., "ja-ro")
   - Normalize codes before using

3. **Handle Missing Translations**
   - Always provide a default title
   - Use fallback chains
   - Don't fail if translation missing

4. **Consider Regional Variations**
   - "en-us" vs "en-gb"
   - "pt" vs "pt-br"
   - "zh-hans" vs "zh-hant"

### For Library Administrators

1. **Set Appropriate Priorities**
   - Match your user base's preferences
   - Include fallback languages
   - Test with sample content

2. **Consider Content Type**
   - Manga: en, ja-ro, ja
   - Manhwa: en, ko-ro, ko
   - Manhua: en, zh
   - Western comics: en, fr, de, es

3. **Update Periodically**
   - Review language settings
   - Adjust based on collection
   - Consider user feedback

## Troubleshooting

### Issue: Wrong language selected

**Cause:** Language priority not set correctly

**Solution:**
```bash
# Check current settings
curl http://localhost:8080/api/v1/libraries/{id}

# Update priority
curl -X PATCH \
  -d '{"titleLanguagePriority": ["en", "ja-ro", "ja"]}' \
  http://localhost:8080/api/v1/libraries/{id}
```

### Issue: Romaji not showing

**Cause:** Romaji not in priority list or `preferRomajiTitles` disabled

**Solution:**
```bash
curl -X PATCH \
  -d '{
    "titleLanguagePriority": ["ja-ro", "en", "ja"],
    "preferRomajiTitles": true
  }' \
  http://localhost:8080/api/v1/libraries/{id}
```

### Issue: Falling back to wrong title

**Cause:** Preferred languages not available in metadata

**Solution:**
- Check what languages your metadata source provides
- Add missing languages to priority list
- Enable `fallbackToOriginalTitle`

## Migration Guide

### From Single-Language to Multi-Language

**Before:**
```kotlin
BookMetadataPatch(
  title = "One Piece"
)
```

**After:**
```kotlin
BookMetadataPatch(
  title = "One Piece", // Fallback
  titles = mapOf(
    "en" to "One Piece",
    "ja" to "ワンピース",
    "ja-ro" to "Wan Pīsu"
  )
)
```

### Updating Existing Plugins

1. Add `titles` field to metadata patches
2. Keep `title` field for backwards compatibility
3. Use builder API for convenience
4. Test with different language priorities

## Performance Considerations

- **Caching:** Title selection is cached per library
- **Memory:** Minimal overhead (just additional map storage)
- **Processing:** Title selection is O(n) where n = priority list length
- **Database:** No schema changes required for existing databases

## Future Enhancements

Planned features:
- [ ] User-specific language overrides
- [ ] Per-series language preferences
- [ ] Automatic language learning from reading history
- [ ] ML-based language detection
- [ ] Translation services integration
- [ ] Synonym and alias management

## Contributing

To improve language support:
1. Add new language codes to `LanguagePriority`
2. Improve detection heuristics
3. Add language-specific sorting rules
4. Contribute translations

## License

Language priority system is part of Komga and follows the same license.
```
