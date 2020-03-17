package org.gotson.komga.interfaces.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Media
import java.time.LocalDate
import java.time.LocalDateTime

data class BookDto(
  val id: Long,
  val seriesId: Long,
  val name: String,
  val url: String,
  val number: Int,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val fileLastModified: LocalDateTime,
  val sizeBytes: Long,
  val size: String,
  val media: MediaDto,
  val metadata: BookMetadataDto
)

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
  val readingDirection: String,
  val readingDirectionLock: Boolean,
  val publisher: String,
  val publisherLock: Boolean,
  val ageRating: Int?,
  val ageRatingLock: Boolean,
  @JsonFormat(pattern = "yyyy-MM-dd")
  val releaseDate: LocalDate?,
  val releaseDateLock: Boolean,
  val authors: List<AuthorDto>,
  val authorsLock: Boolean
)

data class AuthorDto(
  val name: String,
  val role: String
)

fun Book.toDto(includeFullUrl: Boolean) =
  BookDto(
    id = id,
    seriesId = series.id,
    name = name,
    url = if (includeFullUrl) url.toURI().path else FilenameUtils.getName(url.toURI().path),
    number = number,
    created = createdDate?.toUTC(),
    lastModified = lastModifiedDate?.toUTC(),
    fileLastModified = fileLastModified.toUTC(),
    sizeBytes = fileSize,
    size = fileSizeHumanReadable(),
    media = media.toDto(),
    metadata = metadata.toDto()
  )

fun Media.toDto() = MediaDto(
  status = status.toString(),
  mediaType = mediaType ?: "",
  pagesCount = pages.size,
  comment = comment ?: ""
)

fun BookMetadata.toDto() = BookMetadataDto(
  title = title,
  titleLock = titleLock,
  summary = summary,
  summaryLock = summaryLock,
  number = number,
  numberLock = numberLock,
  numberSort = numberSort,
  numberSortLock = numberSortLock,
  readingDirection = readingDirection?.name ?: "",
  readingDirectionLock = readingDirectionLock,
  publisher = publisher,
  publisherLock = publisherLock,
  ageRating = ageRating,
  ageRatingLock = ageRatingLock,
  releaseDate = releaseDate,
  releaseDateLock = releaseDateLock,
  authors = authors.map { it.toDto() },
  authorsLock = authorsLock
)

fun Author.toDto() = AuthorDto(name = name, role = role)
