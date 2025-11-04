package org.gotson.komga.infrastructure.plugin

import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.MultiLanguageTitle
import org.gotson.komga.domain.model.SeriesMetadataPatch

/**
 * Helper extensions for plugins to easily create language-aware metadata patches.
 */

/**
 * Create a BookMetadataPatch with multiple language titles.
 */
fun BookMetadataPatch.Companion.withLanguages(
  titles: Map<String, String>,
  defaultLanguage: String = "en",
  configure: BookMetadataPatch.() -> BookMetadataPatch = { this },
): BookMetadataPatch {
  val defaultTitle = titles[defaultLanguage] ?: titles.values.firstOrNull() ?: ""
  return BookMetadataPatch(
    title = defaultTitle,
    titles = titles,
  ).configure()
}

/**
 * Create a SeriesMetadataPatch with multiple language titles.
 */
fun SeriesMetadataPatch.Companion.withLanguages(
  titles: Map<String, String>,
  defaultLanguage: String = "en",
  configure: SeriesMetadataPatch.() -> SeriesMetadataPatch = { this },
): SeriesMetadataPatch {
  val defaultTitle = titles[defaultLanguage] ?: titles.values.firstOrNull() ?: ""
  return SeriesMetadataPatch(
    title = defaultTitle,
    titles = titles,
  ).configure()
}

/**
 * Extension to add language support to existing BookMetadataPatch.
 */
fun BookMetadataPatch.withLanguageTitles(titles: Map<String, String>): BookMetadataPatch {
  return this.copy(titles = titles)
}

/**
 * Extension to add language support to existing SeriesMetadataPatch.
 */
fun SeriesMetadataPatch.withLanguageTitles(titles: Map<String, String>): SeriesMetadataPatch {
  return this.copy(titles = titles)
}

/**
 * Builder for creating multi-language metadata patches.
 */
class MultiLanguageMetadataBuilder {
  private val titles = mutableMapOf<String, String>()
  private val summaries = mutableMapOf<String, String>()

  fun addTitle(language: String, title: String): MultiLanguageMetadataBuilder {
    titles[language] = title
    return this
  }

  fun addSummary(language: String, summary: String): MultiLanguageMetadataBuilder {
    summaries[language] = summary
    return this
  }

  fun addTitles(languageTitles: Map<String, String>): MultiLanguageMetadataBuilder {
    titles.putAll(languageTitles)
    return this
  }

  fun addSummaries(languageSummaries: Map<String, String>): MultiLanguageMetadataBuilder {
    summaries.putAll(languageSummaries)
    return this
  }

  fun buildForBook(defaultLanguage: String = "en"): BookMetadataPatch {
    val defaultTitle = titles[defaultLanguage] ?: titles.values.firstOrNull() ?: ""
    val defaultSummary = summaries[defaultLanguage] ?: summaries.values.firstOrNull()

    return BookMetadataPatch(
      title = defaultTitle,
      titles = titles.takeIf { it.isNotEmpty() },
      summary = defaultSummary,
      summaries = summaries.takeIf { it.isNotEmpty() },
    )
  }

  fun buildForSeries(defaultLanguage: String = "en"): SeriesMetadataPatch {
    val defaultTitle = titles[defaultLanguage] ?: titles.values.firstOrNull() ?: ""
    val defaultSummary = summaries[defaultLanguage] ?: summaries.values.firstOrNull()

    return SeriesMetadataPatch(
      title = defaultTitle,
      titles = titles.takeIf { it.isNotEmpty() },
      summary = defaultSummary,
      summaries = summaries.takeIf { it.isNotEmpty() },
    )
  }
}

// Companion object extensions for builders
val BookMetadataPatch.Companion.multiLanguage: MultiLanguageMetadataBuilder
  get() = MultiLanguageMetadataBuilder()

val SeriesMetadataPatch.Companion.multiLanguage: MultiLanguageMetadataBuilder
  get() = MultiLanguageMetadataBuilder()
