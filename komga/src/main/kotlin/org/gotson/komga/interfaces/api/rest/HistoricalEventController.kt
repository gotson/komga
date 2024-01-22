package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Parameter
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.interfaces.api.persistence.HistoricalEventDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.HistoricalEventDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/history", produces = [MediaType.APPLICATION_JSON_VALUE])
class HistoricalEventController(
  private val historicalEventDtoRepository: HistoricalEventDtoRepository,
) {
  @GetMapping
  @PageableAsQueryParam
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
