package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SeriesCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesCollectionRepository {
  /**
   * Find one SeriesCollection by [collectionId],
   * seriesId will be filtered by the provided [context] libraries.
   */
  fun findByIdOrNull(
    collectionId: String,
    context: SearchContext,
  ): SeriesCollection?

  /**
   * Find all SeriesCollection
   * optionally with at least one Series belonging to the provided [belongsToLibraryIds] if not null,
   * seriesId will be filtered by the provided [context] libraries.
   */
  fun findAll(
    context: SearchContext,
    pageable: Pageable,
    belongsToLibraryIds: Collection<String>? = null,
    search: String? = null,
  ): Page<SeriesCollection>

  /**
   * Find all SeriesCollection that contains the provided [containsSeriesId],
   * seriesId will be filtered by the provided [context] libraries.
   */
  fun findAllContainingSeriesId(
    containsSeriesId: String,
    context: SearchContext,
  ): Collection<SeriesCollection>

  fun findAllEmpty(): Collection<SeriesCollection>

  fun findByNameOrNull(name: String): SeriesCollection?

  fun insert(collection: SeriesCollection)

  fun update(collection: SeriesCollection)

  fun removeSeriesFromAll(seriesId: String)

  fun removeSeriesFromAll(seriesIds: Collection<String>)

  fun delete(collectionId: String)

  fun delete(collectionIds: Collection<String>)

  fun deleteAll()

  fun existsByName(name: String): Boolean

  fun count(): Long
}
