package org.gotson.komga.domain.model

import java.time.LocalDate

data class BookMetadataPatch(
  val title: String?,
  val summary: String?,
  val number: String?,
  val numberSort: Float?,
  val releaseDate: LocalDate?,
  val authors: List<Author>?,

  val readLists: List<ReadListEntry> = emptyList()
) {
  data class ReadListEntry(
    val name: String,
    val number: Int? = null
  )
}
