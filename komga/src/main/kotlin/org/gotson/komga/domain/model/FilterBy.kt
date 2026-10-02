package org.gotson.komga.domain.model

enum class FilterByEntity {
  LIBRARY,
  COLLECTION,
  SERIES,
  READLIST,
}

data class FilterBy(
  val type: FilterByEntity,
  val ids: Set<String>,
)

enum class FilterTags {
  SERIES,
  BOOK,
  BOTH,
}

/**
 * Convenience function to build [FilterBy] from multiple parameters.
 * The prevalence is as follows:
 * - [FilterByEntity.LIBRARY]
 * - [FilterByEntity.COLLECTION]
 * - [FilterByEntity.SERIES]
 * - [FilterByEntity.READLIST]
 */
fun unpackFilters(
  libraryIds: Set<String> = emptySet(),
  collectionIds: Set<String> = emptySet(),
  seriesIds: Set<String> = emptySet(),
  readListIds: Set<String> = emptySet(),
) = when {
  libraryIds.isNotEmpty() -> FilterBy(FilterByEntity.LIBRARY, libraryIds)
  collectionIds.isNotEmpty() -> FilterBy(FilterByEntity.COLLECTION, collectionIds)
  seriesIds.isNotEmpty() -> FilterBy(FilterByEntity.SERIES, seriesIds)
  readListIds.isNotEmpty() -> FilterBy(FilterByEntity.READLIST, readListIds)
  else -> null
}
