package org.gotson.komga.domain.model

import java.time.LocalDate

data class BookMetadataPatch(
  val title: String? = null,
  val summary: String? = null,
  val number: String? = null,
  val numberSort: Float? = null,
  val releaseDate: LocalDate? = null,
  val authors: List<Author>? = null,
  val isbn: String? = null,
  val links: List<WebLink>? = null,
  val tags: Set<String>? = null,
  val readLists: List<ReadListEntry> = emptyList(),
) {
  data class ReadListEntry(
    val name: String,
    val number: Int? = null,
  )
}

enum class BookMetadataPatchCapability {
  TITLE,
  SUMMARY,
  NUMBER,
  NUMBER_SORT,
  RELEASE_DATE,
  AUTHORS,
  TAGS,
  ISBN,
  READ_LISTS,
  THUMBNAILS,
  LINKS,
}
