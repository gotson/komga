package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.SeriesCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesCollectionRepository {
  fun findByIdOrNull(collectionId: Long): SeriesCollection?
  fun findAll(search: String? = null, pageable: Pageable): Page<SeriesCollection>

  /**
   * Find one SeriesCollection by collectionId,
   * optionally with only seriesId filtered by the provided filterOnLibraryIds.
   */
  fun findByIdOrNull(collectionId: Long, filterOnLibraryIds: Collection<Long>?): SeriesCollection?

  /**
   * Find all SeriesCollection with at least one Series belonging to the provided belongsToLibraryIds,
   * optionally with only seriesId filtered by the provided filterOnLibraryIds.
   */
  fun findAllByLibraries(belongsToLibraryIds: Collection<Long>, filterOnLibraryIds: Collection<Long>?, search: String? = null, pageable: Pageable): Page<SeriesCollection>

  /**
   * Find all SeriesCollection that contains the provided containsSeriesId,
   * optionally with only seriesId filtered by the provided filterOnLibraryIds.
   */
  fun findAllBySeries(containsSeriesId: Long, filterOnLibraryIds: Collection<Long>?): Collection<SeriesCollection>

  fun insert(collection: SeriesCollection): SeriesCollection
  fun update(collection: SeriesCollection)

  fun removeSeriesFromAll(seriesId: Long)

  fun delete(collectionId: Long)
  fun deleteAll()

  fun existsByName(name: String): Boolean
}
