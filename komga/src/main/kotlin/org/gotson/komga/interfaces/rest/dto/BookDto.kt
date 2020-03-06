package org.gotson.komga.interfaces.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
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
  val media: MediaDto
)

data class MediaDto(
  val status: String,
  val mediaType: String,
  val pagesCount: Int,
  val comment: String
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
    media = MediaDto(
      status = media.status.toString(),
      mediaType = media.mediaType ?: "",
      pagesCount = media.pages.size,
      comment = media.comment ?: ""
    )
  )
