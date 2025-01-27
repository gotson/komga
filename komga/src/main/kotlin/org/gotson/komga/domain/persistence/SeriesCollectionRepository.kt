package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.SeriesCollection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesCollectionRepository {
  /**
   * Find one SeriesCollection by [collectionId],
   * optionally with only seriesId filtered by the provided [filterOnLibraryIds] if not null.
   */
  fun findByIdOrNull(
    collectionId: String,
    filterOnLibraryIds: Collection<String>? = null,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): SeriesCollection?

  /**
   * Find all SeriesCollection
   * optionally with at least one Series belonging to the provided [belongsToLibraryIds] if not null,
   * optionally with only seriesId filtered by the provided [filterOnLibraryIds] if not null.
   */
  fun findAll(
    belongsToLibraryIds: Collection<String>? = null,
    filterOnLibraryIds: Collection<String>? = null,
    search: String? = null,
    pageable: Pageable,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): Page<SeriesCollection>

  /**
   * Find all SeriesCollection that contains the provided [containsSeriesId],
   * optionally with only seriesId filtered by the provided [filterOnLibraryIds] if not null.
   */
  fun findAllContainingSeriesId(
    containsSeriesId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions = ContentRestrictions(),
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
