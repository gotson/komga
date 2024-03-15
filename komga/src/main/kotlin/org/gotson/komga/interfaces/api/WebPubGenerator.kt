package org.gotson.komga.interfaces.api

import org.gotson.komga.domain.model.BookPage
import org.gotson.komga.domain.model.EpubTocEntry
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaExtensionEpub
import org.gotson.komga.domain.model.MediaFile
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.ProxyExtension
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.service.BookAnalyzer
import org.gotson.komga.infrastructure.image.ImageConverter
import org.gotson.komga.infrastructure.image.ImageType
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_DIVINA_JSON
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_DIVINA_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_WEBPUB_JSON
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_WEBPUB_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.OpdsLinkRel
import org.gotson.komga.interfaces.api.dto.PROFILE_DIVINA
import org.gotson.komga.interfaces.api.dto.PROFILE_EPUB
import org.gotson.komga.interfaces.api.dto.PROFILE_PDF
import org.gotson.komga.interfaces.api.dto.WPBelongsToDto
import org.gotson.komga.interfaces.api.dto.WPContributorDto
import org.gotson.komga.interfaces.api.dto.WPLinkDto
import org.gotson.komga.interfaces.api.dto.WPMetadataDto
import org.gotson.komga.interfaces.api.dto.WPPublicationDto
import org.gotson.komga.interfaces.api.dto.WPReadingProgressionDto
import org.gotson.komga.interfaces.api.rest.dto.AuthorDto
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.language.toZonedDateTime
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import org.gotson.komga.domain.model.MediaType as KomgaMediaType

@Component
class WebPubGenerator(
  @Qualifier("thumbnailType") private val thumbnailType: ImageType,
  private val imageConverter: ImageConverter,
  private val bookAnalyzer: BookAnalyzer,
  private val mediaRepository: MediaRepository,
  protected val pathSegments: List<String> = listOf("api", "v1"),
) {
  protected fun toBasePublicationDto(bookDto: BookDto): WPPublicationDto {
    val uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment(*pathSegments.toTypedArray())
    return WPPublicationDto(
      mediaType = getDefaultMediaType(),
      context = "https://readium.org/webpub-manifest/context.jsonld",
      metadata = toWPMetadataDto(bookDto).withAuthors(bookDto.metadata.authors),
      links = bookDto.toWPLinkDtos(uriBuilder),
    )
  }

  protected open fun getDefaultMediaType(): MediaType = MEDIATYPE_WEBPUB_JSON

  protected fun buildThumbnailLinkDtos(bookId: String) =
    listOf(
      WPLinkDto(
        href = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment(*pathSegments.toTypedArray()).path("books/$bookId/thumbnail").toUriString(),
        type = thumbnailType.mediaType,
      ),
    )

  fun toManifestDivina(
    bookDto: BookDto,
    media: Media,
    seriesMetadata: SeriesMetadata,
  ): WPPublicationDto {
    val uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment(*pathSegments.toTypedArray())
    return toBasePublicationDto(bookDto).let {
      val pages = if (media.profile == MediaProfile.PDF) bookAnalyzer.getPdfPagesDynamic(media) else media.pages
      it.copy(
        mediaType = MEDIATYPE_DIVINA_JSON,
        metadata = it.metadata.withSeriesMetadata(seriesMetadata).copy(conformsTo = PROFILE_DIVINA),
        readingOrder =
          pages.mapIndexed { index: Int, page: BookPage ->
            WPLinkDto(
              href = uriBuilder.cloneBuilder().path("books/${bookDto.id}/pages/${index + 1}").queryParam("contentNegotiation", "false").toUriString(),
              type = page.mediaType,
              width = page.dimension?.width,
              height = page.dimension?.height,
              alternate =
                if (!recommendedImageMediaTypes.contains(page.mediaType) && imageConverter.canConvertMediaType(page.mediaType, MediaType.IMAGE_JPEG_VALUE))
                  listOf(
                    WPLinkDto(
                      href = uriBuilder.cloneBuilder().path("books/${bookDto.id}/pages/${index + 1}").queryParam("contentNegotiation", "false").queryParam("convert", "jpeg").toUriString(),
                      type = MediaType.IMAGE_JPEG_VALUE,
                      width = page.dimension?.width,
                      height = page.dimension?.height,
                    ),
                  )
                else
                  emptyList(),
            )
          },
        resources = buildThumbnailLinkDtos(bookDto.id),
      )
    }
  }

  fun toManifestPdf(
    bookDto: BookDto,
    media: Media,
    seriesMetadata: SeriesMetadata,
  ): WPPublicationDto {
    val uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment(*pathSegments.toTypedArray())
    return toBasePublicationDto(bookDto).let {
      it.copy(
        mediaType = MEDIATYPE_WEBPUB_JSON,
        metadata = it.metadata.withSeriesMetadata(seriesMetadata).copy(conformsTo = PROFILE_PDF),
        readingOrder =
          List(media.pageCount) { index: Int ->
            WPLinkDto(
              href = uriBuilder.cloneBuilder().path("books/${bookDto.id}/pages/${index + 1}/raw").toUriString(),
              type = KomgaMediaType.PDF.type,
            )
          },
        resources = buildThumbnailLinkDtos(bookDto.id),
      )
    }
  }

  fun toManifestEpub(
    bookDto: BookDto,
    media: Media,
    seriesMetadata: SeriesMetadata,
  ): WPPublicationDto {
    val uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment(*pathSegments.toTypedArray())
    val extension =
      when {
        media.extension is ProxyExtension && media.extension.proxyForType<MediaExtensionEpub>() -> mediaRepository.findExtensionByIdOrNull(media.bookId) as? MediaExtensionEpub
        media.extension is MediaExtensionEpub -> media.extension
        else -> null
      }
    return toBasePublicationDto(bookDto).let { publication ->
      publication.copy(
        mediaType = MEDIATYPE_WEBPUB_JSON,
        metadata =
          publication.metadata.withSeriesMetadata(seriesMetadata).copy(
            conformsTo = PROFILE_EPUB,
            rendition =
              when (extension?.isFixedLayout) {
                true -> mapOf("layout" to "fixed")
                false -> mapOf("layout" to "reflowable")
                else -> emptyMap()
              },
          ),
        readingOrder =
          media.files.filter { it.subType == MediaFile.SubType.EPUB_PAGE }.map {
            WPLinkDto(
              href = uriBuilder.cloneBuilder().path("books/${bookDto.id}/resource/").path(it.fileName).toUriString(),
              type = it.mediaType,
            )
          },
        resources =
          buildThumbnailLinkDtos(bookDto.id) +
            media.files.filter { it.subType == MediaFile.SubType.EPUB_ASSET }.map {
              WPLinkDto(
                href = uriBuilder.cloneBuilder().path("books/${bookDto.id}/resource/").path(it.fileName).toUriString(),
                type = it.mediaType,
              )
            },
        toc = extension?.toc?.map { it.toWPLinkDto(uriBuilder.cloneBuilder().path("books/${bookDto.id}/resource/")) } ?: emptyList(),
        landmarks = extension?.landmarks?.map { it.toWPLinkDto(uriBuilder.cloneBuilder().path("books/${bookDto.id}/resource/")) } ?: emptyList(),
        pageList = extension?.pageList?.map { it.toWPLinkDto(uriBuilder.cloneBuilder().path("books/${bookDto.id}/resource/")) } ?: emptyList(),
      )
    }
  }

  private fun EpubTocEntry.toWPLinkDto(uriBuilder: UriComponentsBuilder): WPLinkDto =
    WPLinkDto(
      title = title,
      href =
        href?.let {
          val fragment = it.substringAfterLast("#", "")
          val h = it.removeSuffix("#$fragment")
          uriBuilder.cloneBuilder().path(h).toUriString() + if (fragment.isNotEmpty()) "#$fragment" else ""
        },
      children = children.map { it.toWPLinkDto(uriBuilder) },
    )

  protected open fun toWPMetadataDto(bookDto: BookDto) =
    WPMetadataDto(
      title = bookDto.metadata.title,
      description = bookDto.metadata.summary,
      numberOfPages = bookDto.media.pagesCount,
      modified = bookDto.lastModified.toZonedDateTime(),
      published = bookDto.metadata.releaseDate,
      subject = bookDto.metadata.tags.toList(),
      identifier = if (bookDto.metadata.isbn.isNotBlank()) "urn:isbn:${bookDto.metadata.isbn}" else null,
      belongsTo =
        WPBelongsToDto(
          series =
            listOf(
              WPContributorDto(
                bookDto.seriesTitle,
                bookDto.metadata.numberSort,
                getBookSeriesLink(bookDto),
              ),
            ),
        ),
    )

  protected open fun getBookSeriesLink(bookDto: BookDto): List<WPLinkDto> = emptyList()

  private fun WPMetadataDto.withSeriesMetadata(seriesMetadata: SeriesMetadata) =
    copy(
      language = seriesMetadata.language,
      readingProgression =
        when (seriesMetadata.readingDirection) {
          SeriesMetadata.ReadingDirection.LEFT_TO_RIGHT -> WPReadingProgressionDto.LTR
          SeriesMetadata.ReadingDirection.RIGHT_TO_LEFT -> WPReadingProgressionDto.RTL
          SeriesMetadata.ReadingDirection.VERTICAL -> WPReadingProgressionDto.TTB
          SeriesMetadata.ReadingDirection.WEBTOON -> WPReadingProgressionDto.TTB
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
    return buildList {
      // most appropriate manifest
      add(WPLinkDto(rel = OpdsLinkRel.SELF, href = uriBuilder.cloneBuilder().path("books/$id/manifest").toUriString(), type = mediaProfileToWebPub(komgaMediaType?.profile)))
      // PDF is also available under the Divina profile / EPUB that are Divina compatible
      if (komgaMediaType?.profile == MediaProfile.PDF || (komgaMediaType?.profile == MediaProfile.EPUB && media.epubDivinaCompatible))
        add(WPLinkDto(href = uriBuilder.cloneBuilder().path("books/$id/manifest/divina").toUriString(), type = MEDIATYPE_DIVINA_JSON_VALUE))
      // main acquisition link
      add(WPLinkDto(rel = OpdsLinkRel.ACQUISITION, type = komgaMediaType?.exportType ?: media.mediaType, href = uriBuilder.cloneBuilder().path("books/$id/file").toUriString()))
    }
  }

  private fun mediaProfileToWebPub(profile: MediaProfile?): String =
    when (profile) {
      MediaProfile.DIVINA -> MEDIATYPE_DIVINA_JSON_VALUE
      MediaProfile.PDF -> MEDIATYPE_WEBPUB_JSON_VALUE
      MediaProfile.EPUB -> MEDIATYPE_WEBPUB_JSON_VALUE
      null -> MEDIATYPE_WEBPUB_JSON_VALUE
    }

  companion object {
    private val wpKnownRoles =
      listOf(
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
    private val recommendedImageMediaTypes = listOf("image/jpeg", "image/png", "image/gif")
  }
}
