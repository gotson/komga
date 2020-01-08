package org.gotson.komga.interfaces.rest

import com.fasterxml.jackson.annotation.JsonFormat
import org.apache.commons.io.FilenameUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.Series
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

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

data class BookDto(
    val id: Long,
    val seriesId: Long,
    val name: String,
    val url: String,
    val number: Float,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val created: LocalDateTime?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastModified: LocalDateTime?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val fileLastModified: LocalDateTime,
    val sizeBytes: Long,
    val size: String,
    @Deprecated("Deprecated since 0.10", ReplaceWith("media"))
    val metadata: MediaDto,
    val media: MediaDto
)

data class MediaDto(
    val status: String,
    val mediaType: String,
    val pagesCount: Int
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
        metadata = MediaDto(
            status = media.status.toString(),
            mediaType = media.mediaType ?: "",
            pagesCount = media.pages.size
        ),
        media = MediaDto(
            status = media.status.toString(),
            mediaType = media.mediaType ?: "",
            pagesCount = media.pages.size
        )
    )

data class PageDto(
    val number: Int,
    val fileName: String,
    val mediaType: String
)

fun LocalDateTime.toUTC(): LocalDateTime =
    atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
