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
  val booksCount: Int
)

fun Series.toDto(includeUrl: Boolean) = SeriesDto(
  id = id,
  libraryId = library.id,
  name = name,
  url = if (includeUrl) url.toURI().path else "",
  created = createdDate?.toUTC(),
  lastModified = lastModifiedDate?.toUTC(),
  fileLastModified = fileLastModified.toUTC(),
  booksCount = books.size
)
