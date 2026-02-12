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
