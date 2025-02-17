package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.gotson.komga.infrastructure.swagger.OpenApiConfiguration
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.interfaces.api.persistence.HistoricalEventDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.HistoricalEventDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/history", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = OpenApiConfiguration.TagNames.HISTORY)
class HistoricalEventController(
  private val historicalEventDtoRepository: HistoricalEventDtoRepository,
) {
  @GetMapping
  @PageableAsQueryParam
  @Operation(summary = "List historical events")
  fun getAll(
    @Parameter(hidden = true) page: Pageable,
  ): Page<HistoricalEventDto> {
    val sort =
      if (page.sort.isSorted)
        page.sort
      else
        Sort.by(Sort.Order.desc("timestamp"))

    val pageRequest =
      PageRequest.of(
        page.pageNumber,
        page.pageSize,
        sort,
      )

    return historicalEventDtoRepository.findAll(pageRequest)
  }
}
