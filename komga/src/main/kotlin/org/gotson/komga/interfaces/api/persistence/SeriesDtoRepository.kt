package org.gotson.komga.interfaces.api.persistence

import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
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
    search: SeriesSearchWithReadProgress,
    userId: String,
    pageable: Pageable,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): Page<SeriesDto>

  fun findAllByCollectionId(
    collectionId: String,
    search: SeriesSearchWithReadProgress,
    userId: String,
    pageable: Pageable,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): Page<SeriesDto>

  fun findAllRecentlyUpdated(
    search: SeriesSearchWithReadProgress,
    userId: String,
    restrictions: ContentRestrictions,
    pageable: Pageable,
  ): Page<SeriesDto>

  fun countByFirstCharacter(
    search: SeriesSearchWithReadProgress,
    userId: String,
    restrictions: ContentRestrictions,
  ): List<GroupCountDto>
}
