package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.openapi.PageableWithoutSortAsQueryParam
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.AuthorDto
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.REFERENTIAL)
class ReferentialController(
  private val referentialRepository: ReferentialRepository,
) {
  @GetMapping("v1/authors")
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

  @PageableWithoutSortAsQueryParam
  @GetMapping("v2/authors")
  @Operation(summary = "List authors", description = "Can be filtered by various criteria")
  fun getAuthors(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "role", required = false) role: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionId: String?,
    @RequestParam(name = "series_id", required = false) seriesId: String?,
    @RequestParam(name = "readlist_id", required = false) readListId: String?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<AuthorDto> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    return when {
      libraryIds.isNotEmpty() -> referentialRepository.findAllAuthorsByNameAndLibraries(search, role, libraryIds, principal.user.getAuthorizedLibraryIds(null), pageRequest)
      collectionId != null -> referentialRepository.findAllAuthorsByNameAndCollection(search, role, collectionId, principal.user.getAuthorizedLibraryIds(null), pageRequest)
      seriesId != null -> referentialRepository.findAllAuthorsByNameAndSeries(search, role, seriesId, principal.user.getAuthorizedLibraryIds(null), pageRequest)
      readListId != null -> referentialRepository.findAllAuthorsByNameAndReadList(search, role, readListId, principal.user.getAuthorizedLibraryIds(null), pageRequest)
      else -> referentialRepository.findAllAuthorsByName(search, role, principal.user.getAuthorizedLibraryIds(null), pageRequest)
    }.map { it.toDto() }
  }

  @GetMapping("v1/authors/names")
  @Operation(summary = "List authors' names")
  fun getAuthorsNames(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", defaultValue = "") search: String,
  ): List<String> = referentialRepository.findAllAuthorsNamesByName(search, principal.user.getAuthorizedLibraryIds(null))

  @GetMapping("v1/authors/roles")
  @Operation(summary = "List authors' roles")
  fun getAuthorsRoles(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<String> = referentialRepository.findAllAuthorsRoles(principal.user.getAuthorizedLibraryIds(null))

  @GetMapping("v1/genres")
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

  @GetMapping("v1/sharing-labels")
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

  @GetMapping("v1/tags")
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

  @GetMapping("v1/tags/book")
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

  @GetMapping("v1/tags/series")
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

  @GetMapping("v1/languages")
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

  @GetMapping("v1/publishers")
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

  @GetMapping("v1/age-ratings")
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

  @GetMapping("v1/series/release-dates")
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
