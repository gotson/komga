package org.gotson.komga.interfaces.api.dto

import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.VERTICAL
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.WEBTOON
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.valueOf
import org.gotson.komga.infrastructure.jooq.toCurrentTimeZone
import org.gotson.komga.interfaces.api.rest.dto.AuthorDto
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.springframework.http.MediaType
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.ZoneId
import java.time.ZonedDateTime

val MEDIATYPE_DIVINA_JSON = MediaType("application", "webpub+json")
const val MEDIATYPE_WEBPUB_JSON_VALUE = "application/webpub+json"
const val MEDIATYPE_DIVINA_JSON_VALUE = "application/divina+json"
const val MEDIATYPE_OPDS_JSON_VALUE = "application/opds+json"
const val PROFILE_DIVINA = "https://readium.org/webpub-manifest/profiles/divina"

val wpKnownRoles = listOf(
  "author",
  "translator",
  "editor",
  "artist",
  "illustrator",
  "letterer",
  "penciler",
  "penciller",
  "colorist",
  "inker",
)

fun WPMetadataDto.withAuthors(authors: List<AuthorDto>): WPMetadataDto {
  val groups = authors.groupBy({ it.role }, { it.name })
  return copy(
    author = groups["author"].orEmpty(),
    translator = groups["translator"].orEmpty(),
    editor = groups["editor"].orEmpty(),
    artist = groups["artist"].orEmpty(),
    illustrator = groups["illustrator"].orEmpty(),
    letterer = groups["letterer"].orEmpty(),
    penciler = groups["penciler"].orEmpty() + groups["penciller"].orEmpty(),
    colorist = groups["colorist"].orEmpty(),
    inker = groups["inker"].orEmpty(),
    // use contributor role for all roles not mentioned above
    contributor = authors.filterNot { wpKnownRoles.contains(it.role) }.map { it.name },
  )
}

val recommendedImageMediaTypes = listOf("image/jpeg", "image/png", "image/gif")

fun BookDto.toWPPublicationDto(canConvertMediaType: (String, String) -> Boolean, media: Media, seriesMetadata: SeriesMetadata? = null, seriesDto: SeriesDto? = null): WPPublicationDto {
  val builder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("api", "v1")
  return WPPublicationDto(
    metadata = WPMetadataDto(
      title = metadata.title,
      description = metadata.summary,
      numberOfPages = media.pages.size,
      conformsTo = PROFILE_DIVINA,
      modified = lastModified.toCurrentTimeZone().atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
      published = metadata.releaseDate,
      subject = metadata.tags.toList(),
      identifier = if (metadata.isbn.isNotBlank()) "urn:isbn:${metadata.isbn}" else null,
      language = seriesMetadata?.language ?: seriesDto?.metadata?.language,
      readingProgression = (seriesMetadata?.readingDirection ?: seriesDto?.metadata?.readingDirection?.let { valueOf(it) })?.let {
        when (it) {
          LEFT_TO_RIGHT -> WPReadingProgressionDto.LTR
          RIGHT_TO_LEFT -> WPReadingProgressionDto.RTL
          VERTICAL -> WPReadingProgressionDto.TTB
          WEBTOON -> WPReadingProgressionDto.TTB
        }
      },
      belongsTo = (seriesMetadata?.title ?: seriesDto?.metadata?.title)?.let { WPBelongsToDto(series = listOf(WPContributorDto(it, metadata.numberSort))) },
    ).withAuthors(metadata.authors),
    links = listOf(
      WPLinkDto(rel = OpdsLinkRel.SELF, href = builder.cloneBuilder().path("books/$id/manifest").toUriString(), type = MEDIATYPE_DIVINA_JSON_VALUE),
      WPLinkDto(rel = OpdsLinkRel.ACQUISITION, type = media.mediaType, href = builder.cloneBuilder().path("books/$id/file").toUriString()),
    ),
    images = listOf(
      WPLinkDto(
        href = builder.cloneBuilder().path("books/$id/thumbnail").toUriString(),
        type = MediaType.IMAGE_JPEG_VALUE,
      ),
    ),
    readingOrder =
    media.pages.mapIndexed { index: Int, page: BookPage ->
      WPLinkDto(
        href = builder.cloneBuilder().path("books/$id/pages/${index + 1}").toUriString(),
        type = page.mediaType,
        width = page.dimension?.width,
        height = page.dimension?.height,
        alternate = if (!recommendedImageMediaTypes.contains(page.mediaType) && canConvertMediaType(page.mediaType, MediaType.IMAGE_JPEG_VALUE)) listOf(
          WPLinkDto(
            href = builder.cloneBuilder().path("books/$id/pages/${index + 1}").queryParam("convert", "jpeg").toUriString(),
            type = MediaType.IMAGE_JPEG_VALUE,
            width = page.dimension?.width,
            height = page.dimension?.height,
          ),
        ) else emptyList(),
      )
    },
  )
}
