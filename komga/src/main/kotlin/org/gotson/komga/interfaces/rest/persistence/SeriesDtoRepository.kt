package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesDtoRepository {
  fun findAll(search: SeriesSearch, userId: Long, pageable: Pageable): Page<SeriesDto>
  fun findRecentlyUpdated(search: SeriesSearch, userId: Long, pageable: Pageable): Page<SeriesDto>
  fun findByIdOrNull(seriesId: Long, userId: Long): SeriesDto?
}
