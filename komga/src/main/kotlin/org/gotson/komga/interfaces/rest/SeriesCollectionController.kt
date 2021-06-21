package org.gotson.komga.interfaces.rest

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mu.KotlinLogging
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesCollection
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.service.SeriesCollectionLifecycle
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.AuthorsAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableWithoutSortAsQueryParam
import org.gotson.komga.infrastructure.web.Authors
import org.gotson.komga.interfaces.rest.dto.CollectionCreationDto
import org.gotson.komga.interfaces.rest.dto.CollectionDto
import org.gotson.komga.interfaces.rest.dto.CollectionUpdateDto
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.gotson.komga.interfaces.rest.dto.restrictUrl
import org.gotson.komga.interfaces.rest.dto.toDto
import org.gotson.komga.interfaces.rest.persistence.SeriesDtoRepository
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.TimeUnit
import javax.validation.Valid

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/collections", produces = [MediaType.APPLICATION_JSON_VALUE])
class SeriesCollectionController(
  private val collectionRepository: SeriesCollectionRepository,
  private val collectionLifecycle: SeriesCollectionLifecycle,
  private val seriesDtoRepository: SeriesDtoRepository
) {

  @PageableWithoutSortAsQueryParam
  @GetMapping
  fun getAll(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable
  ): Page<CollectionDto> {
    val pageRequest =
      if (unpaged) UnpagedSorted(Sort.by(Sort.Order.asc("name")))
      else PageRequest.of(
        page.pageNumber,
        page.pageSize,
        Sort.by(Sort.Order.asc("name"))
      )

    return when {
      principal.user.sharedAllLibraries && libraryIds == null -> collectionRepository.searchAll(searchTerm, pageable = pageRequest)
      principal.user.sharedAllLibraries && libraryIds != null -> collectionRepository.findAllByLibraryIds(libraryIds, null, searchTerm, pageable = pageRequest)
      !principal.user.sharedAllLibraries && libraryIds != null -> collectionRepository.findAllByLibraryIds(libraryIds, principal.user.sharedLibrariesIds, searchTerm, pageable = pageRequest)
      else -> collectionRepository.findAllByLibraryIds(principal.user.sharedLibrariesIds, principal.user.sharedLibrariesIds, searchTerm, pageable = pageRequest)
    }.map { it.toDto() }
  }

  @GetMapping("{id}")
  fun getOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String
  ): CollectionDto =
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))
      ?.toDto()
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["{id}/thumbnail"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getCollectionThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String
  ): ResponseEntity<ByteArray> {
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let {
      return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
        .body(collectionLifecycle.getThumbnailBytes(it))
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun addOne(
    @Valid @RequestBody collection: CollectionCreationDto
  ): CollectionDto =
    try {
      collectionLifecycle.addCollection(
        SeriesCollection(
          name = collection.name,
          ordered = collection.ordered,
          seriesIds = collection.seriesIds
        )
      ).toDto()
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
    }

  @PatchMapping("{id}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateOne(
    @PathVariable id: String,
    @Valid @RequestBody collection: CollectionUpdateDto
  ) {
    collectionRepository.findByIdOrNull(id)?.let { existing ->
      val updated = existing.copy(
        name = collection.name ?: existing.name,
        ordered = collection.ordered ?: existing.ordered,
        seriesIds = collection.seriesIds ?: existing.seriesIds
      )
      try {
        collectionLifecycle.updateCollection(updated)
      } catch (e: DuplicateNameException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @DeleteMapping("{id}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteOne(
    @PathVariable id: String
  ) {
    collectionRepository.findByIdOrNull(id)?.let {
      collectionLifecycle.deleteCollection(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PageableWithoutSortAsQueryParam
  @AuthorsAsQueryParam
  @GetMapping("{id}/series")
  fun getSeriesForCollection(
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
    @RequestParam(name = "release_year", required = false) release_years: List<String>?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) @Authors authors: List<Author>?,
    @Parameter(hidden = true) page: Pageable
  ): Page<SeriesDto> =
    collectionRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { collection ->
      val sort =
        if (collection.ordered) Sort.by(Sort.Order.asc("collection.number"))
        else Sort.by(Sort.Order.asc("metadata.titleSort"))

      val pageRequest =
        if (unpaged) UnpagedSorted(sort)
        else PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort
        )

      val seriesSearch = SeriesSearchWithReadProgress(
        libraryIds = principal.user.getAuthorizedLibraryIds(libraryIds),
        metadataStatus = metadataStatus,
        readStatus = readStatus,
        publishers = publishers,
        languages = languages,
        genres = genres,
        tags = tags,
        ageRatings = ageRatings?.map { it.toIntOrNull() },
        releaseYears = release_years,
        authors = authors
      )

      seriesDtoRepository.findAllByCollectionId(collection.id, seriesSearch, principal.user.id, pageRequest)
        .map { it.restrictUrl(!principal.user.roleAdmin) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}
