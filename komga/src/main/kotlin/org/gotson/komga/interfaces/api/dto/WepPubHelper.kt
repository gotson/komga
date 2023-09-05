package org.gotson.komga.interfaces.api.dto

import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaType.PDF
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.VERTICAL
import org.gotson.komga.domain.model.SeriesMetadata.ReadingDirection.WEBTOON
import org.gotson.komga.infrastructure.jooq.toCurrentTimeZone
import org.gotson.komga.interfaces.api.rest.dto.AuthorDto
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.springframework.http.MediaType
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import java.time.ZoneId
import java.time.ZonedDateTime
import org.gotson.komga.domain.model.MediaType as KMediaType
import org.gotson.komga.domain.model.MediaType.Companion as KomgaMediaType

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

val recommendedImageMediaTypes = listOf("image/jpeg", "image/png", "image/gif")

private fun BookDto.toBasePublicationDto(includeOpdsLinks: Boolean = false): WPPublicationDto {
  val uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("api", "v1")
  return WPPublicationDto(
    mediaType = MEDIATYPE_OPDS_PUBLICATION_JSON,
    context = "https://readium.org/webpub-manifest/context.jsonld",
    metadata = toWPMetadataDto(includeOpdsLinks).withAuthors(metadata.authors),
    links = toWPLinkDtos(uriBuilder),
  )
}

fun BookDto.toOpdsPublicationDto(includeOpdsLinks: Boolean = false): WPPublicationDto {
  return toBasePublicationDto(includeOpdsLinks).copy(images = buildThumbnailLinkDtos(id))
}

private fun buildThumbnailLinkDtos(bookId: String) = listOf(
  WPLinkDto(
    href = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("api", "v1").path("books/$bookId/thumbnail").toUriString(),
    type = MediaType.IMAGE_JPEG_VALUE,
  ),
)

fun BookDto.toManifestDivina(canConvertMediaType: (String, String) -> Boolean, media: Media, seriesMetadata: SeriesMetadata): WPPublicationDto {
  val uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("api", "v1")
  return toBasePublicationDto().let {
    it.copy(
      mediaType = MEDIATYPE_DIVINA_JSON,
      metadata = it.metadata
        .withSeriesMetadata(seriesMetadata)
        .copy(conformsTo = PROFILE_DIVINA),
      readingOrder = media.pages.mapIndexed { index: Int, page: BookPage ->
        WPLinkDto(
          href = uriBuilder.cloneBuilder().path("books/$id/pages/${index + 1}").toUriString(),
          type = page.mediaType,
          width = page.dimension?.width,
          height = page.dimension?.height,
          alternate = if (!recommendedImageMediaTypes.contains(page.mediaType) && canConvertMediaType(page.mediaType, MediaType.IMAGE_JPEG_VALUE)) listOf(
            WPLinkDto(
              href = uriBuilder.cloneBuilder().path("books/$id/pages/${index + 1}").queryParam("convert", "jpeg").toUriString(),
              type = MediaType.IMAGE_JPEG_VALUE,
              width = page.dimension?.width,
              height = page.dimension?.height,
            ),
          ) else emptyList(),
        )
      },
      resources = buildThumbnailLinkDtos(id),
    )
  }
}

fun BookDto.toManifestPdf(media: Media, seriesMetadata: SeriesMetadata): WPPublicationDto {
  val uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("api", "v1")
  return toBasePublicationDto().let {
    it.copy(
      mediaType = MEDIATYPE_WEBPUB_JSON,
      metadata = it.metadata
        .withSeriesMetadata(seriesMetadata)
        .copy(conformsTo = PROFILE_PDF),
      readingOrder = List(media.pages.size) { index: Int ->
        WPLinkDto(
          href = uriBuilder.cloneBuilder().path("books/$id/pages/${index + 1}/raw").toUriString(),
          type = PDF.type,
        )
      },
      resources = buildThumbnailLinkDtos(id),
    )
  }
}

private fun BookDto.toWPMetadataDto(includeOpdsLinks: Boolean = false) = WPMetadataDto(
  title = metadata.title,
  description = metadata.summary,
  numberOfPages = this.media.pagesCount,
  modified = lastModified.toCurrentTimeZone().atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now(),
  published = metadata.releaseDate,
  subject = metadata.tags.toList(),
  identifier = if (metadata.isbn.isNotBlank()) "urn:isbn:${metadata.isbn}" else null,
  belongsTo = WPBelongsToDto(
    series = listOf(
      WPContributorDto(
        seriesTitle,
        metadata.numberSort,
        if (includeOpdsLinks) listOf(
          WPLinkDto(
            href = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("opds", "v2").path("series/$seriesId").toUriString(),
            type = MEDIATYPE_OPDS_JSON_VALUE,
          ),
        ) else emptyList(),
      ),
    ),
  ),
)

private fun WPMetadataDto.withSeriesMetadata(seriesMetadata: SeriesMetadata) =
  copy(
    language = seriesMetadata.language,
    readingProgression = when (seriesMetadata.readingDirection) {
      LEFT_TO_RIGHT -> WPReadingProgressionDto.LTR
      RIGHT_TO_LEFT -> WPReadingProgressionDto.RTL
      VERTICAL -> WPReadingProgressionDto.TTB
      WEBTOON -> WPReadingProgressionDto.TTB
      null -> null
    },
  )

private fun WPMetadataDto.withAuthors(authors: List<AuthorDto>): WPMetadataDto {
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

private fun BookDto.toWPLinkDtos(uriBuilder: UriComponentsBuilder): List<WPLinkDto> {
  val komgaMediaType = KomgaMediaType.fromMediaType(media.mediaType)
  val download = WPLinkDto(rel = OpdsLinkRel.ACQUISITION, type = media.mediaType, href = uriBuilder.cloneBuilder().path("books/$id/file").toUriString())

  return listOfNotNull(
    // most appropriate manifest
    WPLinkDto(rel = OpdsLinkRel.SELF, href = uriBuilder.cloneBuilder().path("books/$id/manifest").toUriString(), type = mediaTypeToWebPub(komgaMediaType)),
    // PDF is also available under the Divina profile
    if (komgaMediaType == PDF) WPLinkDto(href = uriBuilder.cloneBuilder().path("books/$id/manifest/divina").toUriString(), type = MEDIATYPE_DIVINA_JSON_VALUE) else null,
    // main acquisition link
    download,
    // extra acquisition link with a different export type, useful for CBR/CBZ
    komgaMediaType?.let { download.copy(type = it.exportType) },
  ).distinct()
}

private fun mediaTypeToWebPub(mediaType: KMediaType?): String = when (mediaType) {
  KMediaType.ZIP -> MEDIATYPE_DIVINA_JSON_VALUE
  KMediaType.RAR_GENERIC -> MEDIATYPE_DIVINA_JSON_VALUE
  KMediaType.RAR_4 -> MEDIATYPE_DIVINA_JSON_VALUE
  KMediaType.EPUB -> MEDIATYPE_DIVINA_JSON_VALUE
  PDF -> MEDIATYPE_WEBPUB_JSON_VALUE
  null -> MEDIATYPE_WEBPUB_JSON_VALUE
}
