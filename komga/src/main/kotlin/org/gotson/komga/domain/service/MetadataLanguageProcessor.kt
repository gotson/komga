package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.BookMetadataPatch
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.SeriesMetadataPatch
import org.gotson.komga.domain.persistence.LibraryRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Processes metadata patches to apply language preferences.
 */
@Service
class MetadataLanguageProcessor(
  private val languageService: LanguageService,
  private val libraryRepository: LibraryRepository,
) {
  /**
   * Process book metadata patch to select best title based on library language preferences.
   */
  fun processBookMetadata(
    patch: BookMetadataPatch,
    libraryId: String,
  ): BookMetadataPatch {
    // If no language-specific titles, return as-is
    if (patch.titles == null || patch.titles.isEmpty()) {
      return patch
    }

    val library = libraryRepository.findById(libraryId)

    // Select best title based on language priority
    val bestTitle = languageService.selectBestTitle(
      titles = patch.titles,
      languagePriority = library.titleLanguagePriority,
      preferRomaji = library.preferRomajiTitles,
      fallbackToOriginal = library.fallbackToOriginalTitle,
    )

    // Select best summary if multiple languages provided
    val bestSummary = if (patch.summaries != null && patch.summaries.isNotEmpty()) {
      languageService.selectBestTitle(
        titles = patch.summaries,
        languagePriority = library.titleLanguagePriority,
        preferRomaji = false,
        fallbackToOriginal = library.fallbackToOriginalTitle,
      )
    } else {
      patch.summary
    }

    logger.debug {
      "Processed book metadata: selected title '$bestTitle' from ${patch.titles.size} languages"
    }

    return patch.copy(
      title = bestTitle,
      summary = bestSummary,
    )
  }

  /**
   * Process series metadata patch to select best title based on library language preferences.
   */
  fun processSeriesMetadata(
    patch: SeriesMetadataPatch,
    libraryId: String,
  ): SeriesMetadataPatch {
    // If no language-specific titles, return as-is
    if (patch.titles == null || patch.titles.isEmpty()) {
      return patch
    }

    val library = libraryRepository.findById(libraryId)

    // Select best title based on language priority
    val bestTitle = languageService.selectBestTitle(
      titles = patch.titles,
      languagePriority = library.titleLanguagePriority,
      preferRomaji = library.preferRomajiTitles,
      fallbackToOriginal = library.fallbackToOriginalTitle,
    )

    // Select best summary if multiple languages provided
    val bestSummary = if (patch.summaries != null && patch.summaries.isNotEmpty()) {
      languageService.selectBestTitle(
        titles = patch.summaries,
        languagePriority = library.titleLanguagePriority,
        preferRomaji = false,
        fallbackToOriginal = library.fallbackToOriginalTitle,
      )
    } else {
      patch.summary
    }

    logger.debug {
      "Processed series metadata: selected title '$bestTitle' from ${patch.titles.size} languages"
    }

    return patch.copy(
      title = bestTitle,
      summary = bestSummary,
    )
  }

  /**
   * Process library language settings to ensure they're valid.
   */
  fun validateLanguagePriority(languages: List<String>): List<String> {
    return languages
      .map { it.lowercase().trim() }
      .filter { it.isNotEmpty() }
      .distinct()
  }
}
