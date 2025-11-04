package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.model.LanguagePriority
import org.gotson.komga.domain.service.LanguageService
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.LanguageDto
import org.gotson.komga.interfaces.api.rest.dto.TitleSelectionDto
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/languages", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Languages")
class LanguageController(
  private val languageService: LanguageService,
) {
  @GetMapping
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "List supported languages",
    description = "Get list of supported languages for title selection",
  )
  fun getSupportedLanguages(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<LanguageDto> =
    LanguagePriority.LANGUAGE_NAMES.map { (code, name) ->
      LanguageDto(
        code = code,
        name = name,
        isRomaji = LanguagePriority.isRomaji(code),
      )
    }

  @GetMapping("default-priority")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Get default language priority",
    description = "Get the default language priority order",
  )
  fun getDefaultPriority(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<String> = LanguagePriority.DEFAULT_PRIORITY

  @PostMapping("select-title")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Test title selection",
    description = "Test title selection with different language priorities",
  )
  fun selectTitle(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody request: TitleSelectionDto,
  ): Map<String, String> {
    val selectedTitle = languageService.selectBestTitle(
      titles = request.titles,
      languagePriority = request.languagePriority,
      preferRomaji = request.preferRomaji,
      fallbackToOriginal = request.fallbackToOriginal,
    )

    return mapOf(
      "selectedTitle" to selectedTitle,
      "selectedLanguage" to (request.titles.entries.find { it.value == selectedTitle }?.key ?: "unknown"),
    )
  }

  @PostMapping("detect")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Detect language from text",
    description = "Detect the language of a given text",
  )
  fun detectLanguage(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody request: Map<String, String>,
  ): Map<String, String> {
    val text = request["text"] ?: return mapOf("error" to "No text provided")
    val detectedLanguage = languageService.detectLanguage(text)

    return mapOf(
      "text" to text,
      "detectedLanguage" to detectedLanguage,
      "languageName" to (LanguagePriority.LANGUAGE_NAMES[detectedLanguage] ?: "Unknown"),
    )
  }

  @PostMapping("normalize")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Normalize language code",
    description = "Normalize a language code to standard format",
  )
  fun normalizeLanguageCode(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody request: Map<String, String>,
  ): Map<String, String> {
    val code = request["code"] ?: return mapOf("error" to "No code provided")
    val normalized = LanguagePriority.normalize(code)

    return mapOf(
      "original" to code,
      "normalized" to normalized,
      "languageName" to (LanguagePriority.LANGUAGE_NAMES[normalized] ?: "Unknown"),
    )
  }
}
