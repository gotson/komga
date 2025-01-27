package org.gotson.komga.interfaces.api.persistence

import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.interfaces.api.rest.dto.GroupCountDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesDtoRepository {
  fun findByIdOrNull(
    seriesId: String,
    userId: String,
  ): SeriesDto?

  fun findAll(
    pageable: Pageable,
  ): Page<SeriesDto>

  fun findAll(
    context: SearchContext,
    pageable: Pageable,
  ): Page<SeriesDto>

  fun findAll(
    search: SeriesSearch,
    context: SearchContext,
    pageable: Pageable,
  ): Page<SeriesDto>

  fun findAllRecentlyUpdated(
    search: SeriesSearch,
    context: SearchContext,
    pageable: Pageable,
  ): Page<SeriesDto>

  fun countByFirstCharacter(
    search: SeriesSearch,
    context: SearchContext,
  ): List<GroupCountDto>
}
