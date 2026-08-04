package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.model.FilterBy
import org.gotson.komga.domain.model.FilterByEntity
import org.gotson.komga.domain.model.FilterTags
import org.gotson.komga.domain.model.SearchContext
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
@RequestMapping("api/v2", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.REFERENTIAL)
class ReferentialV2Controller(
  private val referentialRepository: ReferentialRepository,
) {
  @PageableWithoutSortAsQueryParam
  @GetMapping("authors")
  @Operation(summary = "List authors", description = "Can be filtered by various criteria")
  fun getAuthors(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "role", required = false) role: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "series_id", required = false) seriesIds: Set<String> = emptySet(),
    @RequestParam(name = "readlist_id", required = false) readListIds: Set<String> = emptySet(),
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

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)
        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        seriesIds.isNotEmpty() -> FilterBy(FilterByEntity.SERIES, seriesIds)
        readListIds.isNotEmpty() -> FilterBy(FilterByEntity.READLIST, readListIds)
        else -> null
      }

    return referentialRepository.findAuthors(SearchContext(principal.user), search, role, filterBy, pageRequest).map { it.toDto() }
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("authors/roles")
  @Operation(summary = "List authors roles", description = "Can be filtered by various criteria")
  fun getAuthorsRoles(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "series_id", required = false) seriesIds: Set<String> = emptySet(),
    @RequestParam(name = "readlist_id", required = false) readListIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)
        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        seriesIds.isNotEmpty() -> FilterBy(FilterByEntity.SERIES, seriesIds)
        readListIds.isNotEmpty() -> FilterBy(FilterByEntity.READLIST, readListIds)
        else -> null
      }

    return referentialRepository.findAuthorsRoles(SearchContext(principal.user), filterBy, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("authors/names")
  @Operation(summary = "List authors names", description = "Can be filtered by various criteria")
  fun getAuthorsNames(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "role", required = false) role: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "series_id", required = false) seriesIds: Set<String> = emptySet(),
    @RequestParam(name = "readlist_id", required = false) readListIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)
        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        seriesIds.isNotEmpty() -> FilterBy(FilterByEntity.SERIES, seriesIds)
        readListIds.isNotEmpty() -> FilterBy(FilterByEntity.READLIST, readListIds)
        else -> null
      }

    return referentialRepository.findAuthorsNames(SearchContext(principal.user), search, role, filterBy, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("genres")
  @Operation(summary = "List genres", description = "Can be filtered by various criteria")
  fun getGenres(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)

        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        else -> null
      }

    return referentialRepository.findGenres(SearchContext(principal.user), search, filterBy, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("sharing-labels")
  @Operation(summary = "List sharing labels", description = "Can be filtered by various criteria")
  fun getSharingLabels(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)
        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        else -> null
      }

    return referentialRepository.findSharingLabels(SearchContext(principal.user), search, filterBy, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("languages")
  @Operation(summary = "List languages", description = "Can be filtered by various criteria")
  fun getLanguages(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)

        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        else -> null
      }

    return referentialRepository.findLanguages(SearchContext(principal.user), search, filterBy, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("publishers")
  @Operation(summary = "List publishers", description = "Can be filtered by various criteria")
  fun getPublishers(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)

        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        else -> null
      }

    return referentialRepository.findPublishers(SearchContext(principal.user), search, filterBy, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("tags")
  @Operation(summary = "List tags", description = "Can be filtered by various criteria")
  fun getTags(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "series_id", required = false) seriesIds: Set<String> = emptySet(),
    @RequestParam(name = "readlist_id", required = false) readListIds: Set<String> = emptySet(),
    @RequestParam(name = "include", required = false) includeTags: FilterTags = FilterTags.BOTH,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)

        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        seriesIds.isNotEmpty() -> FilterBy(FilterByEntity.SERIES, seriesIds)
        readListIds.isNotEmpty() -> FilterBy(FilterByEntity.READLIST, readListIds)
        else -> null
      }

    return referentialRepository.findTags(SearchContext(principal.user), search, filterBy, includeTags, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("series/release-years")
  @Operation(summary = "List series release years", description = "Can be filtered by various criteria")
  fun getSeriesReleaseYears(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<String> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)

        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        else -> null
      }

    return referentialRepository.findSeriesReleaseYears(SearchContext(principal.user), filterBy, pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("age-ratings")
  @Operation(summary = "List age ratings", description = "Can be filtered by various criteria")
  fun getAgeRatings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<Int> {
    val pageRequest =
      if (unpaged)
        Pageable.unpaged()
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
        )

    val filterBy =
      when {
        libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)

        collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
        else -> null
      }

    return referentialRepository.findAgeRatings(SearchContext(principal.user), filterBy, pageRequest)
  }
}
