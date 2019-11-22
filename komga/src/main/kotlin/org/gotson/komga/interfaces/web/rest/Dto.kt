package org.gotson.komga.interfaces.web.rest

import com.fasterxml.jackson.annotation.JsonFormat
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Series
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

data class SeriesDto(
    val id: Long,
    val name: String,
    val url: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModified: LocalDateTime?,
    val booksCount: Int
)

fun Series.toDto() = SeriesDto(
    id = id,
    name = name,
    url = url.toString(),
    lastModified = lastModifiedDate?.toUTC(),
    booksCount = books.size
)

data class BookDto(
    val id: Long,
    val name: String,
    val url: String,
    val number: Float,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModified: LocalDateTime?,
    val sizeBytes: Long,
    val size: String,
    val metadata: BookMetadataDto
)

data class BookMetadataDto(
    val status: String,
    val mediaType: String,
    val pagesCount: Int
)

fun Book.toDto() =
    BookDto(
        id = id,
        name = name,
        url = url.toURI().path,
        number = number,
        lastModified = lastModifiedDate?.toUTC(),
        sizeBytes = fileSize,
        size = fileSizeHumanReadable(),
        metadata = BookMetadataDto(
            status = metadata.status.toString(),
            mediaType = metadata.mediaType ?: "",
            pagesCount = metadata.pages.size
        )
    )

data class PageDto(
    val number: Int,
    val fileName: String,
    val mediaType: String
)

fun LocalDateTime.toUTC(): LocalDateTime =
    atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
