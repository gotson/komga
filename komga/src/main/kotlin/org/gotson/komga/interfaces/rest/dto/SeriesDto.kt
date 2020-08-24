package org.gotson.komga.interfaces.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class SeriesDto(
  val id: String,
  val libraryId: String,
  val name: String,
  val url: String,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val fileLastModified: LocalDateTime,
  val booksCount: Int,
  val booksReadCount: Int,
  val booksUnreadCount: Int,
  val booksInProgressCount: Int,
  val metadata: SeriesMetadataDto
)

fun SeriesDto.restrictUrl(restrict: Boolean) =
  if (restrict) copy(url = "") else this

data class SeriesMetadataDto(
  val status: String,
  val statusLock: Boolean,
  val title: String,
  val titleLock: Boolean,
  val titleSort: String,
  val titleSortLock: Boolean,
  val summary: String,
  val summaryLock: Boolean,
  val readingDirection: String,
  val readingDirectionLock: Boolean,
  val publisher: String,
  val publisherLock: Boolean,
  val ageRating: Int?,
  val ageRatingLock: Boolean,
  val language: String,
  val languageLock: Boolean,
  val genres: Set<String>,
  val genresLock: Boolean,
  val tags: Set<String>,
  val tagsLock: Boolean,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime
)
