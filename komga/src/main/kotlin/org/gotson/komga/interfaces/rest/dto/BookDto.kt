package org.gotson.komga.interfaces.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.jakewharton.byteunits.BinaryByteUnit
import org.apache.commons.io.FilenameUtils
import java.time.LocalDate
import java.time.LocalDateTime

data class BookDto(
  val id: String,
  val seriesId: String,
  val libraryId: String,
  val name: String,
  val url: String,
  val number: Int,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val fileLastModified: LocalDateTime,
  val sizeBytes: Long,
  val size: String = BinaryByteUnit.format(sizeBytes),
  val media: MediaDto,
  val metadata: BookMetadataDto,
  val readProgress: ReadProgressDto? = null
)

fun BookDto.restrictUrl(restrict: Boolean) =
  if (restrict) copy(url = FilenameUtils.getName(url)) else this

data class MediaDto(
  val status: String,
  val mediaType: String,
  val pagesCount: Int,
  val comment: String
)

data class BookMetadataDto(
  val title: String,
  val titleLock: Boolean,
  val summary: String,
  val summaryLock: Boolean,
  val number: String,
  val numberLock: Boolean,
  val numberSort: Float,
  val numberSortLock: Boolean,
  @JsonFormat(pattern = "yyyy-MM-dd")
  val releaseDate: LocalDate?,
  val releaseDateLock: Boolean,
  val authors: List<AuthorDto>,
  val authorsLock: Boolean,
  val tags: Set<String>,
  val tagsLock: Boolean,
  val isbn: String,
  val isbnLock: Boolean,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime
)

data class ReadProgressDto(
  val page: Int,
  val completed: Boolean,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime
)
