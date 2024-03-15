package org.gotson.komga.interfaces.api.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.apache.commons.compress.archivers.zip.Zip64Mode
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import org.gotson.komga.application.tasks.HIGHEST_PRIORITY
import org.gotson.komga.application.tasks.HIGH_PRIORITY
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.AlternateTitle
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.MarkSelectedPreference
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaType.ZIP
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.model.WebLink
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.AuthorsAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableWithoutSortAsQueryParam
import org.gotson.komga.infrastructure.web.Authors
import org.gotson.komga.infrastructure.web.DelimitedPair
import org.gotson.komga.interfaces.api.ContentRestrictionChecker
import org.gotson.komga.interfaces.api.persistence.BookDtoRepository
import org.gotson.komga.interfaces.api.persistence.ReadProgressDtoRepository
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.gotson.komga.interfaces.api.rest.dto.CollectionDto
import org.gotson.komga.interfaces.api.rest.dto.GroupCountDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesMetadataUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.TachiyomiReadProgressUpdateV2Dto
import org.gotson.komga.interfaces.api.rest.dto.TachiyomiReadProgressV2Dto
import org.gotson.komga.interfaces.api.rest.dto.ThumbnailSeriesDto
import org.gotson.komga.interfaces.api.rest.dto.restrictUrl
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.FileSystemResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.Deflater

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
class SeriesController(
  private val taskEmitter: TaskEmitter,
  private val seriesRepository: SeriesRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val readProgressDtoRepository: ReadProgressDtoRepository,
  private val eventPublisher: ApplicationEventPublisher,
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
  private val thumbnailsSeriesRepository: ThumbnailSeriesRepository,
  private val contentRestrictionChecker: ContentRestrictionChecker,
) {
  @PageableAsQueryParam
  @AuthorsAsQueryParam
  @Parameters(
    Parameter(
      description = "Search by regex criteria, in the form: regex,field. Supported fields are TITLE and TITLE_SORT.",
      `in` = ParameterIn.QUERY,
      name = "search_regex",
      schema = Schema(type = "string"),
    ),
  )
  @GetMapping("v1/series")
  fun getAllSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @Parameter(hidden = true)
    @DelimitedPair("search_regex")
    searchRegex: Pair<String, String>? = null,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>? = null,
    @RequestParam(name = "collection_id", required = false) collectionIds: List<String>? = null,
    @RequestParam(name = "status", required = false) metadataStatus: List<SeriesMetadata.Status>? = null,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>? = null,
    @RequestParam(name = "publisher", required = false) publishers: List<String>? = null,
    @RequestParam(name = "language", required = false) languages: List<String>? = null,
    @RequestParam(name = "genre", required = false) genres: List<String>? = null,
    @RequestParam(name = "tag", required = false) tags: List<String>? = null,
    @RequestParam(name = "age_rating", required = false) ageRatings: List<String>? = null,
    @RequestParam(name = "release_year", required = false) releaseYears: List<String>? = null,
    @RequestParam(name = "sharing_label", required = false) sharingLabels: List<String>? = null,
    @RequestParam(name = "deleted", required = false) deleted: Boolean? = null,
    @RequestParam(name = "complete", required = false) complete: Boolean? = null,
    @RequestParam(name = "oneshot", required = false) oneshot: Boolean? = null,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) @Authors authors: List<Author>? = null,
    @Parameter(hidden = true) page: Pageable,
  ): Page<SeriesDto> {
    val sort =
      when {
        page.sort.isSorted -> page.sort
        !searchTerm.isNullOrBlank() -> Sort.by("relevance")
        else -> Sort.unsorted()
      }

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    val seriesSearch =
      SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
        collectionIds = collectionIds,
        searchTerm = searchTerm,
        searchRegex =
          searchRegex?.let {
            when (it.second.lowercase()) {
              "title" -> Pair(it.first, SeriesSearch.SearchField.TITLE)
              "title_sort" -> Pair(it.first, SeriesSearch.SearchField.TITLE_SORT)
              else -> null
            }
          },
        metadataStatus = metadataStatus,
        publishers = publishers,
        deleted = deleted,
        complete = complete,
        oneshot = oneshot,
        languages = languages,
        genres = genres,
        tags = tags,
        ageRatings = ageRatings?.map { it.toIntOrNull() },
        releaseYears = releaseYears,
        readStatus = readStatus,
        authors = authors,
        sharingLabels = sharingLabels,
      )

    return seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageRequest, principal.user.restrictions)
      .map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @AuthorsAsQueryParam
  @Parameters(
    Parameter(
      description = "Search by regex criteria, in the form: regex,field. Supported fields are TITLE and TITLE_SORT.",
      `in` = ParameterIn.QUERY,
      name = "search_regex",
      schema = Schema(type = "string"),
    ),
  )
  @GetMapping("v1/series/alphabetical-groups")
  fun getAlphabeticalGroups(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @Parameter(hidden = true)
    @DelimitedPair("search_regex")
    searchRegex: Pair<String, String>?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "collection_id", required = false) collectionIds: List<String>?,
    @RequestParam(name = "status", required = false) metadataStatus: List<SeriesMetadata.Status>?,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>?,
    @RequestParam(name = "publisher", required = false) publishers: List<String>?,
    @RequestParam(name = "language", required = false) languages: List<String>?,
    @RequestParam(name = "genre", required = false) genres: List<String>?,
    @RequestParam(name = "tag", required = false) tags: List<String>?,
    @RequestParam(name = "age_rating", required = false) ageRatings: List<String>?,
    @RequestParam(name = "release_year", required = false) releaseYears: List<String>?,
    @RequestParam(name = "sharing_label", required = false) sharingLabels: List<String>? = null,
    @RequestParam(name = "deleted", required = false) deleted: Boolean?,
    @RequestParam(name = "complete", required = false) complete: Boolean?,
    @RequestParam(name = "oneshot", required = false) oneshot: Boolean? = null,
    @Parameter(hidden = true) @Authors authors: List<Author>?,
    @Parameter(hidden = true) page: Pageable,
  ): List<GroupCountDto> {
    val seriesSearch =
      SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
        collectionIds = collectionIds,
        searchTerm = searchTerm,
        searchRegex =
          searchRegex?.let {
            when (it.second.lowercase()) {
              "title" -> Pair(it.first, SeriesSearch.SearchField.TITLE)
              "title_sort" -> Pair(it.first, SeriesSearch.SearchField.TITLE_SORT)
              else -> null
            }
          },
        metadataStatus = metadataStatus,
        publishers = publishers,
        deleted = deleted,
        complete = complete,
        oneshot = oneshot,
        languages = languages,
        genres = genres,
        tags = tags,
        ageRatings = ageRatings?.map { it.toIntOrNull() },
        releaseYears = releaseYears,
        readStatus = readStatus,
        authors = authors,
        sharingLabels = sharingLabels,
      )

    return seriesDtoRepository.countByFirstCharacter(seriesSearch, principal.user.id, principal.user.restrictions)
  }

  @Operation(description = "Return recently added or updated series.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("v1/series/latest")
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "deleted", required = false) deleted: Boolean?,
    @RequestParam(name = "oneshot", required = false) oneshot: Boolean? = null,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<SeriesDto> {
    val sort = Sort.by(Sort.Order.desc("lastModified"))

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return seriesDtoRepository.findAll(
      SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
        deleted = deleted,
        oneshot = oneshot,
      ),
      principal.user.id,
      pageRequest,
      principal.user.restrictions,
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return newly added series.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("v1/series/new")
  fun getNewSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>? = null,
    @RequestParam(name = "deleted", required = false) deleted: Boolean? = null,
    @RequestParam(name = "oneshot", required = false) oneshot: Boolean? = null,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<SeriesDto> {
    val sort = Sort.by(Sort.Order.desc("created"))

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return seriesDtoRepository.findAll(
      SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
        deleted = deleted,
        oneshot = oneshot,
      ),
      principal.user.id,
      pageRequest,
      principal.user.restrictions,
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return recently updated series, but not newly added ones.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("v1/series/updated")
  fun getUpdatedSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>? = null,
    @RequestParam(name = "deleted", required = false) deleted: Boolean? = null,
    @RequestParam(name = "oneshot", required = false) oneshot: Boolean? = null,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<SeriesDto> {
    val sort = Sort.by(Sort.Order.desc("lastModified"))

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return seriesDtoRepository.findAllRecentlyUpdated(
      SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
        deleted = deleted,
        oneshot = oneshot,
      ),
      principal.user.id,
      principal.user.restrictions,
      pageRequest,
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @GetMapping("v1/series/{seriesId}")
  fun getOneSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") id: String,
  ): SeriesDto =
    seriesDtoRepository.findByIdOrNull(id, principal.user.id)?.let {
      contentRestrictionChecker.checkContentRestriction(principal.user, it)
      it.restrictUrl(!principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["v1/series/{seriesId}/thumbnail"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getSeriesDefaultThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String,
  ): ByteArray {
    principal.user.checkContentRestriction(seriesId)

    return seriesLifecycle.getThumbnailBytes(seriesId, principal.user.id)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["v1/series/{seriesId}/thumbnails/{thumbnailId}"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getSeriesThumbnailById(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ): ByteArray {
    principal.user.checkContentRestriction(seriesId)

    return seriesLifecycle.getThumbnailBytesByThumbnailId(thumbnailId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping(value = ["v1/series/{seriesId}/thumbnails"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getSeriesThumbnails(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String,
  ): Collection<ThumbnailSeriesDto> {
    principal.user.checkContentRestriction(seriesId)

    return thumbnailsSeriesRepository.findAllBySeriesId(seriesId)
      .map { it.toDto() }
  }

  @PostMapping(value = ["v1/series/{seriesId}/thumbnails"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun postUserUploadedSeriesThumbnail(
    @PathVariable(name = "seriesId") seriesId: String,
    @RequestParam("file") file: MultipartFile,
    @RequestParam("selected") selected: Boolean = true,
  ): ThumbnailSeriesDto {
    val series = seriesRepository.findByIdOrNull(seriesId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    if (series.oneshot) throw ResponseStatusException(HttpStatus.BAD_REQUEST)

    val mediaType = file.inputStream.buffered().use { contentDetector.detectMediaType(it) }
    if (!contentDetector.isImage(mediaType))
      throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE)

    return seriesLifecycle.addThumbnailForSeries(
      ThumbnailSeries(
        seriesId = series.id,
        thumbnail = file.bytes,
        type = ThumbnailSeries.Type.USER_UPLOADED,
        fileSize = file.bytes.size.toLong(),
        mediaType = mediaType,
        dimension = imageAnalyzer.getDimension(file.inputStream.buffered()) ?: Dimension(0, 0),
      ),
      if (selected) MarkSelectedPreference.YES else MarkSelectedPreference.NO,
    ).toDto()
  }

  @PutMapping("v1/series/{seriesId}/thumbnails/{thumbnailId}/selected")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun postMarkSelectedSeriesThumbnail(
    @PathVariable(name = "seriesId") seriesId: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ) {
    seriesRepository.findByIdOrNull(seriesId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    thumbnailsSeriesRepository.findByIdOrNull(thumbnailId)?.let {
      thumbnailsSeriesRepository.markSelected(it)
      eventPublisher.publishEvent(DomainEvent.ThumbnailSeriesAdded(it.copy(selected = true)))
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @DeleteMapping("v1/series/{seriesId}/thumbnails/{thumbnailId}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteUserUploadedSeriesThumbnail(
    @PathVariable(name = "seriesId") seriesId: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ) {
    seriesRepository.findByIdOrNull(seriesId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    thumbnailsSeriesRepository.findByIdOrNull(thumbnailId)?.let {
      try {
        seriesLifecycle.deleteThumbnailForSeries(it)
      } catch (e: IllegalArgumentException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PageableAsQueryParam
  @AuthorsAsQueryParam
  @GetMapping("v1/series/{seriesId}/books")
  fun getAllBooksBySeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String,
    @RequestParam(name = "media_status", required = false) mediaStatus: List<Media.Status>? = null,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>? = null,
    @RequestParam(name = "tag", required = false) tags: List<String>? = null,
    @RequestParam(name = "deleted", required = false) deleted: Boolean? = null,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) @Authors authors: List<Author>? = null,
    @Parameter(hidden = true) page: Pageable,
  ): Page<BookDto> {
    principal.user.checkContentRestriction(seriesId)

    val sort =
      if (page.sort.isSorted)
        page.sort
      else
        Sort.by(Sort.Order.asc("metadata.numberSort"))

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return bookDtoRepository.findAll(
      BookSearchWithReadProgress(
        seriesIds = listOf(seriesId),
        mediaStatus = mediaStatus,
        deleted = deleted,
        readStatus = readStatus,
        tags = tags,
        authors = authors,
      ),
      principal.user.id,
      pageRequest,
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @GetMapping("v1/series/{seriesId}/collections")
  fun getAllCollectionsBySeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String,
  ): List<CollectionDto> {
    principal.user.checkContentRestriction(seriesId)

    return collectionRepository.findAllContainingSeriesId(seriesId, principal.user.getAuthorizedLibraryIds(null), principal.user.restrictions)
      .map { it.toDto() }
  }

  @PostMapping("v1/series/{seriesId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(
    @PathVariable seriesId: String,
  ) {
    taskEmitter.analyzeBook(bookRepository.findAllBySeriesId(seriesId), HIGH_PRIORITY)
  }

  @PostMapping("v1/series/{seriesId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(
    @PathVariable seriesId: String,
  ) {
    val books = bookRepository.findAllBySeriesId(seriesId)
    taskEmitter.refreshBookMetadata(books, priority = HIGH_PRIORITY)
    taskEmitter.refreshBookLocalArtwork(books, priority = HIGH_PRIORITY)
    taskEmitter.refreshSeriesLocalArtwork(seriesId, priority = HIGH_PRIORITY)
  }

  @PatchMapping("v1/series/{seriesId}/metadata")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateMetadata(
    @PathVariable seriesId: String,
    @Parameter(description = "Metadata fields to update. Set a field to null to unset the metadata. You can omit fields you don't want to update.")
    @Valid
    @RequestBody
    newMetadata: SeriesMetadataUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) =
    seriesMetadataRepository.findByIdOrNull(seriesId)?.let { existing ->
      val updated =
        with(newMetadata) {
          existing.copy(
            status = status ?: existing.status,
            statusLock = statusLock ?: existing.statusLock,
            title = title ?: existing.title,
            titleLock = titleLock ?: existing.titleLock,
            titleSort = titleSort ?: existing.titleSort,
            titleSortLock = titleSortLock ?: existing.titleSortLock,
            summary = summary ?: existing.summary,
            summaryLock = summaryLock ?: existing.summaryLock,
            language = language ?: existing.language,
            languageLock = languageLock ?: existing.languageLock,
            readingDirection = if (isSet("readingDirection")) readingDirection else existing.readingDirection,
            readingDirectionLock = readingDirectionLock ?: existing.readingDirectionLock,
            publisher = publisher ?: existing.publisher,
            publisherLock = publisherLock ?: existing.publisherLock,
            ageRating = if (isSet("ageRating")) ageRating else existing.ageRating,
            ageRatingLock = ageRatingLock ?: existing.ageRatingLock,
            genres =
              if (isSet("genres")) {
                if (genres != null) genres!! else emptySet()
              } else {
                existing.genres
              },
            genresLock = genresLock ?: existing.genresLock,
            tags =
              if (isSet("tags")) {
                if (tags != null) tags!! else emptySet()
              } else {
                existing.tags
              },
            tagsLock = tagsLock ?: existing.tagsLock,
            totalBookCount = if (isSet("totalBookCount")) totalBookCount else existing.totalBookCount,
            totalBookCountLock = totalBookCountLock ?: existing.totalBookCountLock,
            sharingLabels =
              if (isSet("sharingLabels")) {
                if (sharingLabels != null) sharingLabels!! else emptySet()
              } else {
                existing.sharingLabels
              },
            sharingLabelsLock = sharingLabelsLock ?: existing.sharingLabelsLock,
            links =
              if (isSet("links")) {
                if (links != null) links!!.map { WebLink(it.label!!, URI(it.url!!)) } else emptyList()
              } else {
                existing.links
              },
            linksLock = linksLock ?: existing.linksLock,
            alternateTitles =
              if (isSet("alternateTitles")) {
                if (alternateTitles != null) alternateTitles!!.map { AlternateTitle(it.label!!, it.title!!) } else emptyList()
              } else {
                existing.alternateTitles
              },
            alternateTitlesLock = alternateTitlesLock ?: existing.alternateTitlesLock,
          )
        }
      seriesMetadataRepository.update(updated)

      seriesRepository.findByIdOrNull(seriesId)?.let { eventPublisher.publishEvent(DomainEvent.SeriesUpdated(it)) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @Operation(description = "Mark all book for series as read")
  @PostMapping("v1/series/{seriesId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAsRead(
    @PathVariable seriesId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    principal.user.checkContentRestriction(seriesId)

    seriesLifecycle.markReadProgressCompleted(seriesId, principal.user)
  }

  @Operation(description = "Mark all book for series as unread")
  @DeleteMapping("v1/series/{seriesId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAsUnread(
    @PathVariable seriesId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    principal.user.checkContentRestriction(seriesId)

    seriesLifecycle.deleteReadProgress(seriesId, principal.user)
  }

  @GetMapping("v2/series/{seriesId}/read-progress/tachiyomi")
  fun getReadProgressTachiyomiV2(
    @PathVariable seriesId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): TachiyomiReadProgressV2Dto {
    principal.user.checkContentRestriction(seriesId)

    return readProgressDtoRepository.findProgressV2BySeries(seriesId, principal.user.id)
  }

  @PutMapping("v2/series/{seriesId}/read-progress/tachiyomi")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markReadProgressTachiyomiV2(
    @PathVariable seriesId: String,
    @RequestBody readProgress: TachiyomiReadProgressUpdateV2Dto,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    principal.user.checkContentRestriction(seriesId)

    bookDtoRepository.findAll(
      BookSearchWithReadProgress(seriesIds = listOf(seriesId)),
      principal.user.id,
      UnpagedSorted(Sort.by(Sort.Order.asc("metadata.numberSort"))),
    ).toList().filter { book -> book.metadata.numberSort <= readProgress.lastBookNumberSortRead }
      .forEach { book ->
        if (book.readProgress?.completed != true)
          bookLifecycle.markReadProgressCompleted(book.id, principal.user)
      }
  }

  @GetMapping("v1/series/{seriesId}/file", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  @PreAuthorize("hasRole('$ROLE_FILE_DOWNLOAD')")
  fun getSeriesFile(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable seriesId: String,
  ): ResponseEntity<StreamingResponseBody> {
    principal.user.checkContentRestriction(seriesId)

    val books = bookRepository.findAllBySeriesId(seriesId)

    val streamingResponse =
      StreamingResponseBody { responseStream: OutputStream ->
        ZipArchiveOutputStream(responseStream).use { zipStream ->
          zipStream.setMethod(ZipArchiveOutputStream.DEFLATED)
          zipStream.setLevel(Deflater.NO_COMPRESSION)
          zipStream.setUseZip64(Zip64Mode.Always)
          books.forEach { book ->
            val file = FileSystemResource(book.path)
            if (!file.exists()) {
              logger.warn { "Book file not found, skipping archive entry: ${file.path}" }
              return@forEach
            }

            logger.debug { "Adding file to zip archive: ${file.path}" }
            file.inputStream.use {
              zipStream.putArchiveEntry(ZipArchiveEntry(file.filename))
              IOUtils.copyLarge(it, zipStream, ByteArray(8192))
              zipStream.closeArchiveEntry()
            }
          }
        }
      }

    return ResponseEntity.ok()
      .headers(
        HttpHeaders().apply {
          contentDisposition =
            ContentDisposition.builder("attachment")
              .filename(seriesMetadataRepository.findById(seriesId).title + ".zip", UTF_8)
              .build()
        },
      )
      .contentType(MediaType.parseMediaType(ZIP.type))
      .body(streamingResponse)
  }

  @DeleteMapping("v1/series/{seriesId}/file")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteSeries(
    @PathVariable seriesId: String,
  ) {
    taskEmitter.deleteSeries(
      seriesId = seriesId,
      priority = HIGHEST_PRIORITY,
    )
  }

  /**
   * Convenience function to check for content restriction.
   * This will retrieve data from repositories if needed.
   *
   * @throws[ResponseStatusException] if the user cannot access the content
   */
  private fun KomgaUser.checkContentRestriction(seriesId: String) {
    if (!sharedAllLibraries) {
      seriesRepository.getLibraryId(seriesId)?.let {
        if (!canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    if (restrictions.isRestricted)
      seriesMetadataRepository.findById(seriesId).let {
        if (!isContentAllowed(it.ageRating, it.sharingLabels)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }
  }
}
