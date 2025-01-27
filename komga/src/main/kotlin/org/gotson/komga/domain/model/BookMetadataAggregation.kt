package org.gotson.komga.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class BookMetadataAggregation(
  val authors: List<Author> = emptyList(),
  val tags: Set<String> = emptySet(),
  val releaseDate: LocalDate? = null,
  val summary: String = "",
  val summaryNumber: String = "",
  val seriesId: String = "",
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable
