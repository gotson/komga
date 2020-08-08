package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesDtoRepository {
  fun findAll(search: SeriesSearchWithReadProgress, userId: String, pageable: Pageable): Page<SeriesDto>
  fun findByCollectionId(collectionId: String, userId: String, pageable: Pageable): Page<SeriesDto>
  fun findRecentlyUpdated(search: SeriesSearchWithReadProgress, userId: String, pageable: Pageable): Page<SeriesDto>
  fun findByIdOrNull(seriesId: String, userId: String): SeriesDto?
}
