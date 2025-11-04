package org.gotson.komga.domain.model

/**
 * Language codes and utilities for title preference.
 */
object LanguagePriority {
  // Common language codes
  const val ENGLISH = "en"
  const val ENGLISH_US = "en-us"
  const val JAPANESE = "ja"
  const val JAPANESE_ROMAJI = "ja-ro"
  const val KOREAN = "ko"
  const val KOREAN_ROMAJI = "ko-ro"
  const val CHINESE_SIMPLIFIED = "zh-hans"
  const val CHINESE_TRADITIONAL = "zh-hant"
  const val CHINESE = "zh"
  const val FRENCH = "fr"
  const val GERMAN = "de"
  const val SPANISH = "es"
  const val ITALIAN = "it"
  const val PORTUGUESE = "pt"
  const val PORTUGUESE_BR = "pt-br"
  const val RUSSIAN = "ru"
  const val THAI = "th"
  const val INDONESIAN = "id"
  const val VIETNAMESE = "vi"

  // Default priority order
  val DEFAULT_PRIORITY = listOf(ENGLISH, ENGLISH_US, JAPANESE_ROMAJI, JAPANESE)

  /**
   * Common language names for UI display
   */
  val LANGUAGE_NAMES = mapOf(
    ENGLISH to "English",
    ENGLISH_US to "English (US)",
    JAPANESE to "Japanese",
    JAPANESE_ROMAJI to "Japanese (Romaji)",
    KOREAN to "Korean",
    KOREAN_ROMAJI to "Korean (Romaji)",
    CHINESE_SIMPLIFIED to "Chinese (Simplified)",
    CHINESE_TRADITIONAL to "Chinese (Traditional)",
    CHINESE to "Chinese",
    FRENCH to "French",
    GERMAN to "German",
    SPANISH to "Spanish",
    ITALIAN to "Italian",
    PORTUGUESE to "Portuguese",
    PORTUGUESE_BR to "Portuguese (Brazil)",
    RUSSIAN to "Russian",
    THAI to "Thai",
    INDONESIAN to "Indonesian",
    VIETNAMESE to "Vietnamese",
  )

  /**
   * Check if a language code is for romaji/romanized text
   */
  fun isRomaji(languageCode: String): Boolean {
    return languageCode.endsWith("-ro") || languageCode.contains("romaji")
  }

  /**
   * Normalize language code to lowercase and handle common variations
   */
  fun normalize(languageCode: String): String {
    val normalized = languageCode.lowercase().trim()

    // Handle common variations
    return when {
      normalized in listOf("eng", "english") -> ENGLISH
      normalized in listOf("jpn", "japanese") -> JAPANESE
      normalized in listOf("romaji", "romanji", "roomaji") -> JAPANESE_ROMAJI
      normalized in listOf("kor", "korean") -> KOREAN
      normalized in listOf("chi", "chn", "chinese") -> CHINESE
      normalized in listOf("zho") -> CHINESE
      normalized in listOf("fra", "fre") -> FRENCH
      normalized in listOf("deu", "ger") -> GERMAN
      normalized in listOf("spa") -> SPANISH
      normalized in listOf("ita") -> ITALIAN
      normalized in listOf("por") -> PORTUGUESE
      normalized in listOf("rus") -> RUSSIAN
      normalized in listOf("tha") -> THAI
      normalized in listOf("ind") -> INDONESIAN
      normalized in listOf("vie") -> VIETNAMESE
      else -> normalized
    }
  }

  /**
   * Check if two language codes match, considering variations
   */
  fun matches(code1: String, code2: String): Boolean {
    val norm1 = normalize(code1)
    val norm2 = normalize(code2)

    // Exact match
    if (norm1 == norm2) return true

    // Language family match (e.g., "en" matches "en-us")
    val base1 = norm1.substringBefore("-")
    val base2 = norm2.substringBefore("-")

    return base1 == base2
  }
}

/**
 * Represents a title in multiple languages
 */
data class MultiLanguageTitle(
  val titles: Map<String, String>,
  val defaultTitle: String,
) {
  /**
   * Get the best title based on language priority
   */
  fun getBestTitle(languagePriority: List<String>, preferRomaji: Boolean = false): String {
    // If preferRomaji is true, prioritize romaji titles
    if (preferRomaji) {
      val romajiTitle = titles.entries.find { LanguagePriority.isRomaji(it.key) }
      if (romajiTitle != null) return romajiTitle.value
    }

    // Try each language in priority order
    for (lang in languagePriority) {
      // Try exact match first
      titles[lang]?.let { return it }

      // Try normalized match
      val normalizedLang = LanguagePriority.normalize(lang)
      titles[normalizedLang]?.let { return it }

      // Try base language match (e.g., "en" for "en-us")
      val baseLang = lang.substringBefore("-")
      titles.entries.find { it.key.startsWith(baseLang) }?.let { return it.value }
    }

    // Fallback to default
    return defaultTitle
  }

  companion object {
    /**
     * Create from a map of titles with a specified default
     */
    fun from(titles: Map<String, String>, defaultTitle: String? = null): MultiLanguageTitle {
      val default = defaultTitle ?: titles.values.firstOrNull() ?: ""
      return MultiLanguageTitle(titles, default)
    }

    /**
     * Create from a single title
     */
    fun single(title: String, language: String = "en"): MultiLanguageTitle {
      return MultiLanguageTitle(mapOf(language to title), title)
    }
  }
}
