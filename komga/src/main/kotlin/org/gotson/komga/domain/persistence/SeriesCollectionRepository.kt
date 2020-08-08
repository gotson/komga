package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.SeriesCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesCollectionRepository {
  fun findByIdOrNull(collectionId: String): SeriesCollection?
  fun findAll(search: String? = null, pageable: Pageable): Page<SeriesCollection>

  /**
   * Find one SeriesCollection by collectionId,
   * optionally with only seriesId filtered by the provided filterOnLibraryIds.
   */
  fun findByIdOrNull(collectionId: String, filterOnLibraryIds: Collection<String>?): SeriesCollection?

  /**
   * Find all SeriesCollection with at least one Series belonging to the provided belongsToLibraryIds,
   * optionally with only seriesId filtered by the provided filterOnLibraryIds.
   */
  fun findAllByLibraries(belongsToLibraryIds: Collection<String>, filterOnLibraryIds: Collection<String>?, search: String? = null, pageable: Pageable): Page<SeriesCollection>

  /**
   * Find all SeriesCollection that contains the provided containsSeriesId,
   * optionally with only seriesId filtered by the provided filterOnLibraryIds.
   */
  fun findAllBySeries(containsSeriesId: String, filterOnLibraryIds: Collection<String>?): Collection<SeriesCollection>

  fun findByNameOrNull(name: String): SeriesCollection?

  fun insert(collection: SeriesCollection)
  fun update(collection: SeriesCollection)

  fun removeSeriesFromAll(seriesId: String)
  fun removeSeriesFromAll(seriesIds: Collection<String>)

  fun delete(collectionId: String)

  fun deleteAll()
  fun existsByName(name: String): Boolean
}
