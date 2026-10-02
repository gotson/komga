package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.application.query.PageResponse
import org.gotson.komga.application.query.toPageResponse
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.unpackFilters
import org.gotson.komga.domain.persistence.ReferentialRepository
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.openapi.PageableWithoutSortAsQueryParam
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.NotableEntityDto
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.gotson.komga.interfaces.api.unpackPageParams
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v3", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = OpenApiConfiguration.TagNames.REFERENTIAL)
class ReferentialV3Controller(
  private val referentialRepository: ReferentialRepository,
) {
  @PageableWithoutSortAsQueryParam
  @GetMapping("notable-entities")
  @Operation(summary = "List notable entities", description = "Can be filtered by various criteria")
  fun getNotableEntities(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "kind", required = false) kind: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "series_id", required = false) seriesIds: Set<String> = emptySet(),
    @RequestParam(name = "readlist_id", required = false) readListIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): PageResponse<NotableEntityDto> {
    val pageRequest = unpackPageParams(unpaged, page)
    val filterBy = unpackFilters(libraryIds, collectionIds, seriesIds, readListIds)

    return referentialRepository.findNotableEntities(SearchContext(principal.user), search, kind, filterBy, pageRequest).toPageResponse(pageRequest) { it.toDto() }
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("notable-entities/kinds")
  @Operation(summary = "List notable entities kinds", description = "Can be filtered by various criteria")
  fun getNotableEntitiesKinds(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "series_id", required = false) seriesIds: Set<String> = emptySet(),
    @RequestParam(name = "readlist_id", required = false) readListIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): PageResponse<String> {
    val pageRequest = unpackPageParams(unpaged, page)
    val filterBy = unpackFilters(libraryIds, collectionIds, seriesIds, readListIds)

    return referentialRepository.findNotableEntitiesKinds(SearchContext(principal.user), filterBy, pageRequest).toPageResponse(pageRequest)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("notable-entities/names")
  @Operation(summary = "List notable entities names", description = "Can be filtered by various criteria")
  fun getNotableEntitiesNames(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) search: String?,
    @RequestParam(name = "kind", required = false) kind: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: Set<String> = emptySet(),
    @RequestParam(name = "collection_id", required = false) collectionIds: Set<String> = emptySet(),
    @RequestParam(name = "series_id", required = false) seriesIds: Set<String> = emptySet(),
    @RequestParam(name = "readlist_id", required = false) readListIds: Set<String> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): PageResponse<String> {
    val pageRequest = unpackPageParams(unpaged, page)
    val filterBy = unpackFilters(libraryIds, collectionIds, seriesIds, readListIds)

    return referentialRepository.findNotableEntitiesNames(SearchContext(principal.user), search, kind, filterBy, pageRequest).toPageResponse(pageRequest)
  }
}
