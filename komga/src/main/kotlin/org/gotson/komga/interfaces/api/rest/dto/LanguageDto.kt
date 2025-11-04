package org.gotson.komga.interfaces.api.rest.dto

data class LanguageDto(
  val code: String,
  val name: String,
  val isRomaji: Boolean = false,
)

data class TitleSelectionDto(
  val titles: Map<String, String>,
  val languagePriority: List<String>,
  val preferRomaji: Boolean = false,
  val fallbackToOriginal: Boolean = true,
)

data class LibraryLanguageSettingsDto(
  val titleLanguagePriority: List<String>,
  val preferRomajiTitles: Boolean,
  val fallbackToOriginalTitle: Boolean,
)

data class LanguageDetectionDto(
  val text: String,
  val detectedLanguage: String,
  val confidence: Double? = null,
)
