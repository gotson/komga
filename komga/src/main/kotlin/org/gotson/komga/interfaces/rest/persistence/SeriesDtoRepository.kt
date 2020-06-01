package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesDtoRepository {
  fun findAll(search: SeriesSearch, pageable: Pageable): Page<SeriesDto>
  fun findRecentlyUpdated(search: SeriesSearch, pageable: Pageable): Page<SeriesDto>
  fun findByIdOrNull(seriesId: Long): SeriesDto?
}
