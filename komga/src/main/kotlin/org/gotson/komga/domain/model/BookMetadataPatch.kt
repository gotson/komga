package org.gotson.komga.domain.model

import java.time.LocalDate

data class BookMetadataPatch(
  val title: String?,
  val summary: String?,
  val number: String?,
  val numberSort: Float?,
  val readingDirection: BookMetadata.ReadingDirection?,
  val publisher: String?,
  val ageRating: Int?,
  val releaseDate: LocalDate?,
  val authors: List<Author>?,
  val series: SeriesMetadataPatch?
)
