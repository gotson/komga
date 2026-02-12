package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.AuthorDto
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.REFERENTIAL)
class ReferentialV1Controller(
  private val referentialRepository: ReferentialRepository,
) {
  @GetMapping("authors")
  @Deprecated("Use GET /v2/authors instead", ReplaceWith("getAuthors"))
  @Operation(summary = "List authors", description = "Use GET /api/v2/authors instead. Deprecated since 1.20.0.", tags = [OpenApiConfiguration.TagNames.DEPRECATED])
  fun getAuthorsDeprecated(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", defaultValue = "") search: String,
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
    @RequestParam(name = "series_id", required = false) seriesId: String?,
  ): List<AuthorDto> =

    when {
      libraryId != null -> referentialRepository.findAllAuthorsByNameAndLibrary(search, libraryId, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllAuthorsByNameAndCollection(search, collectionId, principal.user.getAuthorizedLibraryIds(null))
      seriesId != null -> referentialRepository.findAllAuthorsByNameAndSeries(search, seriesId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllAuthorsByName(search, principal.user.getAuthorizedLibraryIds(null))
    }.map { it.toDto() }

  @GetMapping("authors/names")
  @Operation(summary = "List authors' names")
  fun getAuthorsNames(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", defaultValue = "") search: String,
  ): List<String> = referentialRepository.findAllAuthorsNamesByName(search, principal.user.getAuthorizedLibraryIds(null))

  @GetMapping("authors/roles")
  @Operation(summary = "List authors' roles")
  fun getAuthorsRoles(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<String> = referentialRepository.findAllAuthorsRoles(principal.user.getAuthorizedLibraryIds(null))

  @GetMapping("genres")
  @Operation(summary = "List genres", description = "Can be filtered by various criteria")
  fun getGenres(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllGenresByLibraries(libraryIds, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllGenresByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllGenres(principal.user.getAuthorizedLibraryIds(null))
    }

  @GetMapping("sharing-labels")
  @Operation(summary = "List sharing labels", description = "Can be filtered by various criteria")
  fun getSharingLabels(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllSharingLabelsByLibraries(libraryIds, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllSharingLabelsByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllSharingLabels(principal.user.getAuthorizedLibraryIds(null))
    }

  @GetMapping("tags")
  @Operation(summary = "List tags", description = "Can be filtered by various criteria")
  fun getTags(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllSeriesAndBookTagsByLibraries(libraryIds, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllSeriesAndBookTagsByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllSeriesAndBookTags(principal.user.getAuthorizedLibraryIds(null))
    }

  @GetMapping("tags/book")
  @Operation(summary = "List book tags", description = "Can be filtered by various criteria")
  fun getBookTags(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "series_id", required = false) seriesId: String?,
    @RequestParam(name = "readlist_id", required = false) readListId: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
  ): Set<String> =
    when {
      seriesId != null -> referentialRepository.findAllBookTagsBySeries(seriesId, principal.user.getAuthorizedLibraryIds(null))
      readListId != null -> referentialRepository.findAllBookTagsByReadList(readListId, principal.user.getAuthorizedLibraryIds(null))
      libraryIds.isNotEmpty() -> referentialRepository.findAllBookTags(principal.user.getAuthorizedLibraryIds(libraryIds))
      else -> referentialRepository.findAllBookTags(principal.user.getAuthorizedLibraryIds(null))
    }

  @GetMapping("tags/series")
  @Operation(summary = "List series tags", description = "Can be filtered by various criteria")
  fun getSeriesTags(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryId: String?,
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryId != null -> referentialRepository.findAllSeriesTagsByLibrary(libraryId, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllSeriesTagsByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllSeriesTags(principal.user.getAuthorizedLibraryIds(null))
    }

  @GetMapping("languages")
  @Operation(summary = "List languages", description = "Can be filtered by various criteria")
  fun getLanguages(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllLanguagesByLibraries(libraryIds, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllLanguagesByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllLanguages(principal.user.getAuthorizedLibraryIds(null))
    }

  @GetMapping("publishers")
  @Operation(summary = "List publishers", description = "Can be filtered by various criteria")
  fun getPublishers(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllPublishersByLibraries(libraryIds, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllPublishersByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllPublishers(principal.user.getAuthorizedLibraryIds(null))
    }

  @GetMapping("age-ratings")
  @Operation(summary = "List age ratings", description = "Can be filtered by various criteria")
  fun getAgeRatings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllAgeRatingsByLibraries(libraryIds, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllAgeRatingsByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllAgeRatings(principal.user.getAuthorizedLibraryIds(null))
    }.map { it?.toString() ?: "None" }.toSet()

  @GetMapping("series/release-dates")
  @Operation(summary = "List series release dates", description = "Can be filtered by various criteria")
  fun getSeriesReleaseDates(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
  ): Set<String> =
    when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllSeriesReleaseDatesByLibraries(libraryIds, principal.user.getAuthorizedLibraryIds(null))
      collectionId != null -> referentialRepository.findAllSeriesReleaseDatesByCollection(collectionId, principal.user.getAuthorizedLibraryIds(null))
      else -> referentialRepository.findAllSeriesReleaseDates(principal.user.getAuthorizedLibraryIds(null))
    }.map { it.year.toString() }.toSet()
}
