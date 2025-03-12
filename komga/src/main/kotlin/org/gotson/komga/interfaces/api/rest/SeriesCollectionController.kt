package org.gotson.komga.interfaces.api.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.Dimension
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchContext
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.domain.model.ThumbnailSeriesCollection
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesCollectionRepository
import org.gotson.komga.domain.service.SeriesCollectionLifecycle
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.openapi.AuthorsAsQueryParam
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.openapi.PageableWithoutSortAsQueryParam
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.web.Authors
import org.gotson.komga.interfaces.api.persistence.SeriesDtoRepository
import org.gotson.komga.interfaces.api.rest.dto.CollectionCreationDto
import org.gotson.komga.interfaces.api.rest.dto.CollectionDto
import org.gotson.komga.interfaces.api.rest.dto.CollectionUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.SeriesDto
import org.gotson.komga.interfaces.api.rest.dto.ThumbnailSeriesCollectionDto
import org.gotson.komga.interfaces.api.rest.dto.restrictUrl
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.CacheControl
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
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/collections", produces = [MediaType.APPLICATION_JSON_VALUE])
class SeriesCollectionController(
  private val collectionRepository: SeriesCollectionRepository,
  private val collectionLifecycle: SeriesCollectionLifecycle,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
  private val thumbnailSeriesCollectionRepository: ThumbnailSeriesCollectionRepository,
  private val eventPublisher: ApplicationEventPublisher,
) {
  @Operation(summary = "List collections", tags = [OpenApiConfiguration.TagNames.COLLECTIONS])
  @PageableWithoutSortAsQueryParam
  @GetMapping
  fun getCollections(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<CollectionDto> {
    val sort =
      when {
        !searchTerm.isNullOrBlank() -> Sort.by("relevance")
        else -> Sort.by(Sort.Order.asc("name"))
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

    return collectionRepository
      .findAll(principal.user.getAuthorizedLibraryIds(libraryIds), principal.user.getAuthorizedLibraryIds(null), searchTerm, pageRequest, principal.user.restrictions)
      .map { it.toDto() }
  }

  @Operation(summary = "Get collection details", tags = [OpenApiConfiguration.TagNames.COLLECTIONS])
  @GetMapping("{id}")
  fun getCollectionById(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
  ): CollectionDto =
    collectionRepository
      .findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null), principal.user.restrictions)
      ?.toDto()
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @Operation(summary = "Get collection's poster image", tags = [OpenApiConfiguration.TagNames.COLLECTION_POSTER])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["{id}/thumbnail"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getCollectionThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
  ): ResponseEntity<ByteArray> {
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null), principal.user.restrictions)?.let {
      return ResponseEntity
        .ok()
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
        .body(collectionLifecycle.getThumbnailBytes(it, principal.user.id))
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "Get collection poster image", tags = [OpenApiConfiguration.TagNames.COLLECTION_POSTER])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["{id}/thumbnails/{thumbnailId}"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getCollectionThumbnailById(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id") id: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ): ByteArray {
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null), principal.user.restrictions)?.let {
      return collectionLifecycle.getThumbnailBytes(thumbnailId)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "List collection's posters", tags = [OpenApiConfiguration.TagNames.COLLECTION_POSTER])
  @GetMapping(value = ["{id}/thumbnails"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getCollectionThumbnails(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id") id: String,
  ): Collection<ThumbnailSeriesCollectionDto> {
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null), principal.user.restrictions)?.let {
      return thumbnailSeriesCollectionRepository.findAllByCollectionId(id).map { it.toDto() }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "Add collection poster", tags = [OpenApiConfiguration.TagNames.COLLECTION_POSTER])
  @PostMapping(value = ["{id}/thumbnails"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @PreAuthorize("hasRole('ADMIN')")
  fun addUserUploadedCollectionThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id") id: String,
    @RequestParam("file") file: MultipartFile,
    @RequestParam("selected") selected: Boolean = true,
  ): ThumbnailSeriesCollectionDto {
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { collection ->

      val mediaType = file.inputStream.buffered().use { contentDetector.detectMediaType(it) }
      if (!contentDetector.isImage(mediaType))
        throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE)

      return collectionLifecycle
        .addThumbnail(
          ThumbnailSeriesCollection(
            collectionId = collection.id,
            thumbnail = file.bytes,
            type = ThumbnailSeriesCollection.Type.USER_UPLOADED,
            selected = selected,
            fileSize = file.bytes.size.toLong(),
            mediaType = mediaType,
            dimension = imageAnalyzer.getDimension(file.inputStream.buffered()) ?: Dimension(0, 0),
          ),
        ).toDto()
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "Mark collection poster as selected", tags = [OpenApiConfiguration.TagNames.COLLECTION_POSTER])
  @PutMapping("{id}/thumbnails/{thumbnailId}/selected")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun markCollectionThumbnailSelected(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id") id: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ) {
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let {
      thumbnailSeriesCollectionRepository.findByIdOrNull(thumbnailId)?.let {
        collectionLifecycle.markSelectedThumbnail(it)
        eventPublisher.publishEvent(DomainEvent.ThumbnailSeriesCollectionAdded(it.copy(selected = true)))
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "Delete collection poster", tags = [OpenApiConfiguration.TagNames.COLLECTION_POSTER])
  @DeleteMapping("{id}/thumbnails/{thumbnailId}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteUserUploadedCollectionThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "id") id: String,
    @PathVariable(name = "thumbnailId") thumbnailId: String,
  ) {
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let {
      thumbnailSeriesCollectionRepository.findByIdOrNull(thumbnailId)?.let {
        collectionLifecycle.deleteThumbnail(it)
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "Create collection", tags = [OpenApiConfiguration.TagNames.COLLECTIONS])
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  fun createCollection(
    @Valid @RequestBody
    collection: CollectionCreationDto,
  ): CollectionDto =
    try {
      collectionLifecycle
        .addCollection(
          SeriesCollection(
            name = collection.name,
            ordered = collection.ordered,
            seriesIds = collection.seriesIds,
          ),
        ).toDto()
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
    }

  @Operation(summary = "Update collection", tags = [OpenApiConfiguration.TagNames.COLLECTIONS])
  @PatchMapping("{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateCollectionById(
    @PathVariable id: String,
    @Valid @RequestBody
    collection: CollectionUpdateDto,
  ) {
    collectionRepository.findByIdOrNull(id)?.let { existing ->
      val updated =
        existing.copy(
          name = collection.name ?: existing.name,
          ordered = collection.ordered ?: existing.ordered,
          seriesIds = collection.seriesIds ?: existing.seriesIds,
        )
      try {
        collectionLifecycle.updateCollection(updated)
      } catch (e: DuplicateNameException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "Delete collection", tags = [OpenApiConfiguration.TagNames.COLLECTIONS])
  @DeleteMapping("{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteCollectionById(
    @PathVariable id: String,
  ) {
    collectionRepository.findByIdOrNull(id)?.let {
      collectionLifecycle.deleteCollection(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @Operation(summary = "List collection's series", tags = [OpenApiConfiguration.TagNames.COLLECTION_SERIES])
  @PageableWithoutSortAsQueryParam
  @AuthorsAsQueryParam
  @GetMapping("{id}/series")
  fun getSeriesByCollectionId(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "status", required = false) metadataStatus: List<SeriesMetadata.Status>?,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>?,
    @RequestParam(name = "publisher", required = false) publishers: List<String>?,
    @RequestParam(name = "language", required = false) languages: List<String>?,
    @RequestParam(name = "genre", required = false) genres: List<String>?,
    @RequestParam(name = "tag", required = false) tags: List<String>?,
    @RequestParam(name = "age_rating", required = false) ageRatings: List<String>?,
    @RequestParam(name = "release_year", required = false) releaseYears: List<String>?,
    @RequestParam(name = "deleted", required = false) deleted: Boolean?,
    @RequestParam(name = "complete", required = false) complete: Boolean?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) @Authors authors: List<Author>?,
    @Parameter(hidden = true) page: Pageable,
  ): Page<SeriesDto> =
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null), principal.user.restrictions)?.let { collection ->
      val sort =
        if (collection.ordered)
          Sort.by(Sort.Order.asc("collection.number"))
        else
          Sort.by(Sort.Order.asc("metadata.titleSort"))

      val pageRequest =
        if (unpaged)
          UnpagedSorted(sort)
        else
          PageRequest.of(
            page.pageNumber,
            page.pageSize,
            sort,
          )

      val search =
        SeriesSearch(
          SearchCondition.AllOfSeries(
            buildList {
              add(SearchCondition.CollectionId(SearchOperator.Is(collection.id)))
              if (!libraryIds.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(libraryIds.map { SearchCondition.LibraryId(SearchOperator.Is(it)) }))
              deleted?.let { add(SearchCondition.Deleted(if (it) SearchOperator.IsTrue else SearchOperator.IsFalse)) }
              complete?.let { add(SearchCondition.Complete(if (it) SearchOperator.IsTrue else SearchOperator.IsFalse)) }
              if (!metadataStatus.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(metadataStatus.map { SearchCondition.SeriesStatus(SearchOperator.Is(it)) }))
              if (!publishers.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(publishers.map { SearchCondition.Publisher(SearchOperator.Is(it)) }))
              if (!languages.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(languages.map { SearchCondition.Language(SearchOperator.Is(it)) }))
              if (!tags.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(tags.map { SearchCondition.Tag(SearchOperator.Is(it)) }))
              if (!genres.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(genres.map { SearchCondition.Genre(SearchOperator.Is(it)) }))
              if (!ageRatings.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(ageRatings.map { it.toIntOrNull()?.let { ageRating -> SearchCondition.AgeRating(SearchOperator.Is(ageRating)) } ?: SearchCondition.AgeRating(SearchOperator.IsNullT()) }))
              if (!releaseYears.isNullOrEmpty())
                add(
                  SearchCondition.AnyOfSeries(
                    releaseYears.mapNotNull { it.toIntOrNull() }.map { releaseYear ->
                      SearchCondition.AllOfSeries(
                        SearchCondition.ReleaseDate(SearchOperator.After(ZonedDateTime.of(releaseYear - 1, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC))),
                        SearchCondition.ReleaseDate(SearchOperator.Before(ZonedDateTime.of(releaseYear + 1, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))),
                      )
                    },
                  ),
                )
              if (!readStatus.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(readStatus.map { SearchCondition.ReadStatus(SearchOperator.Is(it)) }))
              if (!authors.isNullOrEmpty()) add(SearchCondition.AnyOfSeries(authors.map { SearchCondition.Author(SearchOperator.Is(SearchCondition.AuthorMatch(it.name, it.role))) }))
            },
          ),
        )

      seriesDtoRepository
        .findAll(search, SearchContext(principal.user), pageRequest)
        .map { it.restrictUrl(!principal.user.isAdmin) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}
