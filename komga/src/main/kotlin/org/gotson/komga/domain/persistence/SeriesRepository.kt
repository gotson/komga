package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesSearch
import java.net.URL

interface SeriesRepository {
  fun findByIdOrNull(seriesId: String): Series?
  fun findNotDeletedByLibraryIdAndUrlOrNull(libraryId: String, url: URL): Series?

  fun findAll(): Collection<Series>
  fun findAllByLibraryId(libraryId: String): Collection<Series>
  fun findAllNotDeletedByLibraryIdAndUrlNotIn(libraryId: String, urls: Collection<URL>): Collection<Series>
  fun findAllByTitleContaining(title: String): Collection<Series>
  fun findAll(search: SeriesSearch): Collection<Series>

  fun getLibraryId(seriesId: String): String?

  fun findAllIdsByLibraryId(libraryId: String): Collection<String>

  fun insert(series: Series)
  fun update(series: Series, updateModifiedTime: Boolean = true)

  fun delete(seriesId: String)
  fun delete(seriesIds: Collection<String>)
  fun deleteAll()

  fun count(): Long
  fun countGroupedByLibraryId(): Map<String, Int>
}
