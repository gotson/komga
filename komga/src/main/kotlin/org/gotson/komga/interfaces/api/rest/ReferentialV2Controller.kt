package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.domain.model.FilterBy
import org.gotson.komga.domain.model.FilterByEntity
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
      libraryIds.isNotEmpty() -> referentialRepository.findAuthors(SearchContext(principal.user), search, role, FilterBy(FilterByEntity.LIBRARY, libraryIds), pageRequest)
      collectionId != null -> referentialRepository.findAuthors(SearchContext(principal.user), search, role, FilterBy(FilterByEntity.COLLECTION, setOf(collectionId)), pageRequest)
      seriesId != null -> referentialRepository.findAuthors(SearchContext(principal.user), search, role, FilterBy(FilterByEntity.SERIES, setOf(seriesId)), pageRequest)
      readListId != null -> referentialRepository.findAuthors(SearchContext(principal.user), search, role, FilterBy(FilterByEntity.READLIST, setOf(readListId)), pageRequest)
      else -> referentialRepository.findAuthors(SearchContext(principal.user), search, role, null, pageRequest)
    }.map { it.toDto() }
  }
}
