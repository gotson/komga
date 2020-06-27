package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesDtoRepository {
  fun findAll(search: SeriesSearchWithReadProgress, userId: Long, pageable: Pageable): Page<SeriesDto>
  fun findByCollectionId(collectionId: Long, userId: Long, pageable: Pageable): Page<SeriesDto>
  fun findRecentlyUpdated(search: SeriesSearchWithReadProgress, userId: Long, pageable: Pageable): Page<SeriesDto>
  fun findByIdOrNull(seriesId: Long, userId: Long): SeriesDto?
}
