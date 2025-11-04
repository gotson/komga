package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.model.ReaderSettings
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.PagePreloadDto
import org.gotson.komga.interfaces.api.rest.dto.ReaderSettingsDto
import org.gotson.komga.interfaces.api.rest.dto.ReaderSettingsUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.patch
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api/v1/reader", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Reader")
class ReaderController {
  // In-memory storage for demo - in production, this should use a repository
  private val settingsStore = mutableMapOf<String, MutableMap<String, ReaderSettings>>()

  @GetMapping("settings")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Get global reader settings",
    description = "Get reader settings for the current user (global, not series-specific)",
  )
  fun getGlobalSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): ReaderSettingsDto {
    val userSettings = settingsStore.getOrPut(principal.user.id) { mutableMapOf() }
    val globalSettings = userSettings.getOrPut("global") {
      ReaderSettings(userId = principal.user.id, seriesId = null)
    }
    return globalSettings.toDto()
  }

  @GetMapping("settings/series/{seriesId}")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Get series-specific reader settings",
    description = "Get reader settings for a specific series, falls back to global if not set",
  )
  fun getSeriesSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable seriesId: String,
  ): ReaderSettingsDto {
    val userSettings = settingsStore.getOrPut(principal.user.id) { mutableMapOf() }
    val seriesSettings = userSettings[seriesId]
      ?: userSettings["global"]
      ?: ReaderSettings(userId = principal.user.id, seriesId = null)

    return seriesSettings.toDto()
  }

  @PatchMapping("settings")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Update global reader settings",
    description = "Update global reader settings for the current user",
  )
  fun updateGlobalSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody update: ReaderSettingsUpdateDto,
  ): ReaderSettingsDto {
    val userSettings = settingsStore.getOrPut(principal.user.id) { mutableMapOf() }
    val currentSettings = userSettings.getOrPut("global") {
      ReaderSettings(userId = principal.user.id, seriesId = null)
    }

    val updated = currentSettings.patch(update)
    userSettings["global"] = updated

    return updated.toDto()
  }

  @PatchMapping("settings/series/{seriesId}")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Update series-specific reader settings",
    description = "Update reader settings for a specific series",
  )
  fun updateSeriesSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable seriesId: String,
    @RequestBody update: ReaderSettingsUpdateDto,
  ): ReaderSettingsDto {
    val userSettings = settingsStore.getOrPut(principal.user.id) { mutableMapOf() }
    val currentSettings = userSettings[seriesId]
      ?: ReaderSettings(userId = principal.user.id, seriesId = seriesId)

    val updated = currentSettings.patch(update)
    userSettings[seriesId] = updated

    return updated.toDto()
  }

  @DeleteMapping("settings/series/{seriesId}")
  @PreAuthorize("hasRole('USER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Delete series-specific reader settings",
    description = "Delete reader settings for a specific series, will fall back to global",
  )
  fun deleteSeriesSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable seriesId: String,
  ) {
    val userSettings = settingsStore[principal.user.id]
    userSettings?.remove(seriesId)
  }

  @PostMapping("preload")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Preload pages",
    description = "Hint to the server to preload specific pages for better performance",
  )
  fun preloadPages(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody preload: PagePreloadDto,
  ) {
    // This is a hint to the server to potentially cache these pages
    // The actual implementation would depend on caching strategy
    // For now, this is a no-op that clients can call
  }

  @GetMapping("modes")
  @PreAuthorize("hasRole('USER')")
  @Operation(
    summary = "Get available reading modes",
    description = "Get list of available reading modes and scale types",
  )
  fun getAvailableModes(): Map<String, List<String>> {
    return mapOf(
      "readingModes" to ReaderSettings.ReadingMode.values().map { it.name },
      "scaleTypes" to ReaderSettings.ScaleType.values().map { it.name },
    )
  }
}
