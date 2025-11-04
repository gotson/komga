package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.model.AgeRating
import org.gotson.komga.domain.service.ContentRestrictionService
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.AgeRatingDto
import org.gotson.komga.interfaces.api.rest.dto.AnonymousAccessSettingsDto
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/anonymous", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Anonymous Access")
class AnonymousAccessController(
  private val contentRestrictionService: ContentRestrictionService,
) {
  @GetMapping("status")
  @Operation(
    summary = "Get anonymous access status",
    description = "Check if current user is anonymous and what content they can access",
  )
  fun getAnonymousStatus(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): Map<String, Any> {
    val user = principal.user
    val accessibleLibraries = contentRestrictionService.getAccessibleLibraries(user)

    return mapOf(
      "isAnonymous" to user.isAnonymous(),
      "canAccessAdultContent" to user.canAccessAdultContent(),
      "accessibleLibraryCount" to accessibleLibraries.size,
      "accessibleLibraryIds" to accessibleLibraries.map { it.id },
    )
  }

  @GetMapping("settings")
  @Operation(
    summary = "Get anonymous access settings",
    description = "Get settings for anonymous access across all accessible libraries",
  )
  fun getAnonymousSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<AnonymousAccessSettingsDto> {
    val user = principal.user
    val accessibleLibraries = contentRestrictionService.getAccessibleLibraries(user)

    return accessibleLibraries.map { library ->
      AnonymousAccessSettingsDto(
        libraryId = library.id,
        libraryName = library.name,
        allowAnonymousAccess = library.allowAnonymousAccess,
        hideAdultContentForAnonymous = library.hideAdultContentForAnonymous,
        ageRestrictionForAnonymous = library.ageRestrictionForAnonymous,
      )
    }
  }

  @GetMapping("age-ratings")
  @Operation(
    summary = "Get available age ratings",
    description = "List all age rating categories and their requirements",
  )
  fun getAgeRatings(): List<AgeRatingDto> {
    return AgeRating.values().map { rating ->
      AgeRatingDto(
        code = rating.name,
        minimumAge = rating.minimumAge,
        requiresAuth = rating.requiresAuthentication(),
        isAdultContent = rating.isAdultContent(),
        isExplicitContent = rating.isExplicitContent(),
      )
    }
  }

  @GetMapping("requires-auth")
  @Operation(
    summary = "Check if authentication is required",
    description = "Determine if the current user needs to authenticate for full access",
  )
  fun requiresAuthentication(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): Map<String, Boolean> {
    val user = principal.user

    return mapOf(
      "isAuthenticated" to !user.isAnonymous(),
      "requiresAuthForAdultContent" to user.isAnonymous(),
      "hasFullAccess" to !user.isAnonymous(),
    )
  }
}
