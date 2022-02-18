package org.gotson.komga.interfaces.api.persistence

import org.gotson.komga.interfaces.api.rest.dto.HistoricalEventDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface HistoricalEventDtoRepository {
  fun findAll(pageable: Pageable): Page<HistoricalEventDto>
}
