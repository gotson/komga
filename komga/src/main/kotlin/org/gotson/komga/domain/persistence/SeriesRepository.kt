package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.Series
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URL

interface SeriesRepository {
  fun findByIdOrNull(seriesId: String): Series?

  fun findNotDeletedByLibraryIdAndUrlOrNull(
    libraryId: String,
    url: URL,
  ): Series?

  fun findAll(): Collection<Series>

  fun findAllByLibraryId(libraryId: String): Collection<Series>

  fun findAllNotDeletedByLibraryIdAndUrlNotIn(
    libraryId: String,
    urls: Collection<URL>,
  ): Collection<Series>

  fun findAllByTitleContaining(title: String): Collection<Series>

  fun findAll(
    searchCondition: SearchCondition.Series?,
    searchContext: SearchContext,
    pageable: Pageable,
  ): Page<Series>

  fun getLibraryId(seriesId: String): String?

  fun findAllIdsByLibraryId(libraryId: String): Collection<String>

  fun insert(series: Series)

  fun update(
    series: Series,
    updateModifiedTime: Boolean = true,
  )

  fun delete(seriesId: String)

  fun delete(seriesIds: Collection<String>)

  fun deleteAll()

  fun count(): Long

  fun countGroupedByLibraryId(): Map<String, Int>
}
