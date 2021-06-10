package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesDtoRepository {
  fun findByIdOrNull(seriesId: String, userId: String): SeriesDto?

  fun findAll(search: SeriesSearchWithReadProgress, userId: String, pageable: Pageable): Page<SeriesDto>
  fun findAllByCollectionId(collectionId: String, search: SeriesSearchWithReadProgress, userId: String, pageable: Pageable): Page<SeriesDto>
  fun findAllRecentlyUpdated(search: SeriesSearchWithReadProgress, userId: String, pageable: Pageable): Page<SeriesDto>
}
