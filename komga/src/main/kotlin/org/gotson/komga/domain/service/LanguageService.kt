package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.LanguagePriority
import org.gotson.komga.domain.model.MultiLanguageTitle
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Service for handling language-aware title selection and metadata processing.
 */
@Service
class LanguageService {
  /**
   * Select the best title from multiple language options based on priority.
   */
  fun selectBestTitle(
    titles: Map<String, String>,
    languagePriority: List<String>,
    preferRomaji: Boolean = false,
    fallbackToOriginal: Boolean = true,
  ): String {
    if (titles.isEmpty()) {
      return ""
    }

    val multiLangTitle = MultiLanguageTitle.from(titles)
    val bestTitle = multiLangTitle.getBestTitle(languagePriority, preferRomaji)

    logger.debug {
      "Selected title '$bestTitle' from ${titles.size} options with priority: $languagePriority"
    }

    return bestTitle
  }

  /**
   * Detect language from title text (basic heuristic).
   * This is a simple implementation - for production, consider using a proper language detection library.
   */
  fun detectLanguage(text: String): String {
    // Japanese characters (Hiragana, Katakana, Kanji)
    if (text.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' || it in '\u4E00'..'\u9FFF' }) {
      // Check if it's likely romaji (mostly ASCII with some Japanese)
      val asciiRatio = text.count { it.code < 128 } / text.length.toDouble()
      return if (asciiRatio > 0.7) LanguagePriority.JAPANESE_ROMAJI else LanguagePriority.JAPANESE
    }

    // Korean characters (Hangul)
    if (text.any { it in '\uAC00'..'\uD7AF' }) {
      return LanguagePriority.KOREAN
    }

    // Chinese characters (CJK Unified Ideographs)
    if (text.any { it in '\u4E00'..'\u9FFF' }) {
      // Simplified vs Traditional detection would require more sophisticated analysis
      return LanguagePriority.CHINESE
    }

    // Thai characters
    if (text.any { it in '\u0E00'..'\u0E7F' }) {
      return LanguagePriority.THAI
    }

    // Cyrillic (Russian, etc.)
    if (text.any { it in '\u0400'..'\u04FF' }) {
      return LanguagePriority.RUSSIAN
    }

    // Default to English for ASCII text
    return LanguagePriority.ENGLISH
  }

  /**
   * Parse language-tagged titles from metadata.
   * Supports formats like "Title [en]", "Title (English)", etc.
   */
  fun parseTitlesFromMetadata(rawTitles: List<String>): Map<String, String> {
    val titleMap = mutableMapOf<String, String>()

    rawTitles.forEach { raw ->
      // Try to extract language tag
      val languageTagRegex = Regex("""\[([a-z]{2}(-[a-z]{2})?)\]$""", RegexOption.IGNORE_CASE)
      val languageNameRegex = Regex("""\(([A-Za-z]+)\)$""")

      val languageTagMatch = languageTagRegex.find(raw)
      val languageNameMatch = languageNameRegex.find(raw)

      when {
        languageTagMatch != null -> {
          val language = LanguagePriority.normalize(languageTagMatch.groupValues[1])
          val title = raw.replace(languageTagMatch.value, "").trim()
          titleMap[language] = title
        }
        languageNameMatch != null -> {
          val languageName = languageNameMatch.groupValues[1].lowercase()
          val language = LanguagePriority.normalize(languageName)
          val title = raw.replace(languageNameMatch.value, "").trim()
          titleMap[language] = title
        }
        else -> {
          // No explicit language tag, try to detect
          val detectedLang = detectLanguage(raw)
          titleMap[detectedLang] = raw
        }
      }
    }

    return titleMap
  }

  /**
   * Normalize title based on preferences.
   */
  fun normalizeTitle(
    title: String,
    removeArticles: Boolean = false,
    normalizeWhitespace: Boolean = true,
  ): String {
    var normalized = title

    // Normalize whitespace
    if (normalizeWhitespace) {
      normalized = normalized.replace(Regex("\\s+"), " ").trim()
    }

    // Remove leading articles (The, A, An)
    if (removeArticles) {
      normalized = normalized.replace(Regex("^(The|A|An)\\s+", RegexOption.IGNORE_CASE), "")
    }

    return normalized
  }

  /**
   * Get language-aware sort key for titles.
   */
  fun getSortKey(
    title: String,
    language: String = LanguagePriority.ENGLISH,
  ): String {
    val normalized = normalizeTitle(title, removeArticles = true)

    // For Japanese/Korean/Chinese, might want to use different collation
    // For now, just return normalized
    return normalized.lowercase()
  }

  /**
   * Compare titles considering language and sorting rules.
   */
  fun compareTitles(
    title1: String,
    title2: String,
    language: String = LanguagePriority.ENGLISH,
  ): Int {
    val key1 = getSortKey(title1, language)
    val key2 = getSortKey(title2, language)
    return key1.compareTo(key2)
  }
}
