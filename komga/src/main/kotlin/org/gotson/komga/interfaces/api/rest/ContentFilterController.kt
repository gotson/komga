package org.gotson.komga.interfaces.api.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.service.ContentFilterService
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.AddToBlacklistDto
import org.gotson.komga.interfaces.api.rest.dto.ContentBlacklistDto
import org.gotson.komga.interfaces.api.rest.dto.ContentBlacklistUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.FilterSummaryDto
import org.gotson.komga.interfaces.api.rest.dto.RemoveFromBlacklistDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

private val logger = KotlinLogging.logger {}

/**
 * REST controller for managing content filtering and blacklists.
 * Allows users to blacklist tags and genres they don't want to see.
 */
@RestController
@RequestMapping("api/v1/users/me/content-filter", produces = [MediaType.APPLICATION_JSON_VALUE])
class ContentFilterController(
  private val userRepository: KomgaUserRepository,
  private val contentFilterService: ContentFilterService,
) {
  /**
   * Get the current user's content blacklist.
   */
  @GetMapping("blacklist")
  fun getBlacklist(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): ContentBlacklistDto {
    val user = userRepository.findByIdOrNull(principal.user.id)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    return ContentBlacklistDto(
      tagsExclude = user.restrictions.tagsExclude,
      genresExclude = user.restrictions.genresExclude,
    )
  }

  /**
   * Update the user's content blacklist (replaces existing blacklist).
   */
  @PutMapping("blacklist")
  fun updateBlacklist(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody update: ContentBlacklistUpdateDto,
  ): ContentBlacklistDto {
    val user = userRepository.findByIdOrNull(principal.user.id)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    logger.info { "Updating blacklist for user ${user.email}" }

    val updatedRestrictions = ContentRestrictions(
      ageRestriction = user.restrictions.ageRestriction,
      labelsAllow = user.restrictions.labelsAllow,
      labelsExclude = user.restrictions.labelsExclude,
      tagsExclude = update.tagsExclude ?: user.restrictions.tagsExclude,
      genresExclude = update.genresExclude ?: user.restrictions.genresExclude,
    )

    val updatedUser = user.copy(restrictions = updatedRestrictions)
    userRepository.update(updatedUser)

    return ContentBlacklistDto(
      tagsExclude = updatedRestrictions.tagsExclude,
      genresExclude = updatedRestrictions.genresExclude,
    )
  }

  /**
   * Patch the user's content blacklist (partial update).
   */
  @PatchMapping("blacklist")
  fun patchBlacklist(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody update: ContentBlacklistUpdateDto,
  ): ContentBlacklistDto {
    return updateBlacklist(principal, update)
  }

  /**
   * Add a single item to the blacklist.
   */
  @PostMapping("blacklist/add")
  fun addToBlacklist(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody item: AddToBlacklistDto,
  ): ContentBlacklistDto {
    val user = userRepository.findByIdOrNull(principal.user.id)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    logger.info { "Adding ${item.type} '${item.value}' to blacklist for user ${user.email}" }

    val updatedRestrictions = when (item.type.lowercase()) {
      "tag" -> {
        ContentRestrictions(
          ageRestriction = user.restrictions.ageRestriction,
          labelsAllow = user.restrictions.labelsAllow,
          labelsExclude = user.restrictions.labelsExclude,
          tagsExclude = user.restrictions.tagsExclude + item.value,
          genresExclude = user.restrictions.genresExclude,
        )
      }
      "genre" -> {
        ContentRestrictions(
          ageRestriction = user.restrictions.ageRestriction,
          labelsAllow = user.restrictions.labelsAllow,
          labelsExclude = user.restrictions.labelsExclude,
          tagsExclude = user.restrictions.tagsExclude,
          genresExclude = user.restrictions.genresExclude + item.value,
        )
      }
      else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type: ${item.type}. Must be 'tag' or 'genre'")
    }

    val updatedUser = user.copy(restrictions = updatedRestrictions)
    userRepository.update(updatedUser)

    return ContentBlacklistDto(
      tagsExclude = updatedRestrictions.tagsExclude,
      genresExclude = updatedRestrictions.genresExclude,
    )
  }

  /**
   * Remove a single item from the blacklist.
   */
  @DeleteMapping("blacklist/remove")
  fun removeFromBlacklist(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody item: RemoveFromBlacklistDto,
  ): ContentBlacklistDto {
    val user = userRepository.findByIdOrNull(principal.user.id)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    logger.info { "Removing ${item.type} '${item.value}' from blacklist for user ${user.email}" }

    val updatedRestrictions = when (item.type.lowercase()) {
      "tag" -> {
        ContentRestrictions(
          ageRestriction = user.restrictions.ageRestriction,
          labelsAllow = user.restrictions.labelsAllow,
          labelsExclude = user.restrictions.labelsExclude,
          tagsExclude = user.restrictions.tagsExclude - item.value,
          genresExclude = user.restrictions.genresExclude,
        )
      }
      "genre" -> {
        ContentRestrictions(
          ageRestriction = user.restrictions.ageRestriction,
          labelsAllow = user.restrictions.labelsAllow,
          labelsExclude = user.restrictions.labelsExclude,
          tagsExclude = user.restrictions.tagsExclude,
          genresExclude = user.restrictions.genresExclude - item.value,
        )
      }
      else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type: ${item.type}. Must be 'tag' or 'genre'")
    }

    val updatedUser = user.copy(restrictions = updatedRestrictions)
    userRepository.update(updatedUser)

    return ContentBlacklistDto(
      tagsExclude = updatedRestrictions.tagsExclude,
      genresExclude = updatedRestrictions.genresExclude,
    )
  }

  /**
   * Clear all blacklists (tags and genres).
   */
  @DeleteMapping("blacklist")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun clearBlacklist(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    val user = userRepository.findByIdOrNull(principal.user.id)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    logger.info { "Clearing all blacklists for user ${user.email}" }

    val updatedRestrictions = ContentRestrictions(
      ageRestriction = user.restrictions.ageRestriction,
      labelsAllow = user.restrictions.labelsAllow,
      labelsExclude = user.restrictions.labelsExclude,
      tagsExclude = emptySet(),
      genresExclude = emptySet(),
    )

    val updatedUser = user.copy(restrictions = updatedRestrictions)
    userRepository.update(updatedUser)
  }

  /**
   * Get filter summary for the current user.
   */
  @GetMapping("summary")
  fun getFilterSummary(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): FilterSummaryDto {
    val user = userRepository.findByIdOrNull(principal.user.id)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    val summary = contentFilterService.getFilterSummary(user)

    return FilterSummaryDto(
      hasRestrictions = summary.hasRestrictions,
      tagsBlacklisted = summary.tagsBlacklisted,
      genresBlacklisted = summary.genresBlacklisted,
      ageRestricted = summary.ageRestricted,
      labelsRestricted = summary.labelsRestricted,
    )
  }
}
