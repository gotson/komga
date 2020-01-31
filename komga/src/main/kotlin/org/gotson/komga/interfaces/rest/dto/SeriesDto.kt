package org.gotson.komga.interfaces.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.gotson.komga.domain.model.Series
import java.time.LocalDateTime

data class SeriesDto(
  val id: Long,
  val libraryId: Long,
  val name: String,
  val url: String,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val fileLastModified: LocalDateTime,
  val booksCount: Int,
  val metadata: SeriesMetadataDto
)

data class SeriesMetadataDto(
  val status: String,
  val statusLock: Boolean,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val created: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModified: LocalDateTime?,
  val title: String,
  val titleLock: Boolean,
  val titleSort: String,
  val titleSortLock: Boolean
)

fun Series.toDto(includeUrl: Boolean) = SeriesDto(
  id = id,
  libraryId = library.id,
  name = name,
  url = if (includeUrl) url.toURI().path else "",
  created = createdDate?.toUTC(),
  lastModified = lastModifiedDate?.toUTC(),
  fileLastModified = fileLastModified.toUTC(),
  booksCount = books.size,
  metadata = SeriesMetadataDto(
    status = metadata.status.name,
    statusLock = metadata.statusLock,
    created = metadata.createdDate?.toUTC(),
    lastModified = metadata.lastModifiedDate?.toUTC(),
    title = metadata.title,
    titleLock = metadata.titleLock,
    titleSort = metadata.titleSort,
    titleSortLock = metadata.titleSortLock
  )
)
