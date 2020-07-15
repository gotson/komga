package org.gotson.komga.interfaces.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableWithoutSortAsQueryParam
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.gotson.komga.interfaces.rest.dto.CollectionDto
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.gotson.komga.interfaces.rest.dto.SeriesMetadataUpdateDto
import org.gotson.komga.interfaces.rest.dto.restrictUrl
import org.gotson.komga.interfaces.rest.dto.toDto
import org.gotson.komga.interfaces.rest.persistence.BookDtoRepository
import org.gotson.komga.interfaces.rest.persistence.SeriesDtoRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
import javax.validation.Valid

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/series", produces = [MediaType.APPLICATION_JSON_VALUE])
class SeriesController(
  private val taskReceiver: TaskReceiver,
  private val seriesRepository: SeriesRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val bookController: BookController,
  private val collectionRepository: SeriesCollectionRepository
) {

  @PageableAsQueryParam
  @GetMapping
  fun getAllSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "collection_id", required = false) collectionIds: List<Long>?,
    @RequestParam(name = "status", required = false) metadataStatus: List<SeriesMetadata.Status>?,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable
  ): Page<SeriesDto> {
    val sort =
      if (page.sort.isSorted) page.sort
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
      collectionIds = collectionIds,
      searchTerm = searchTerm,
      metadataStatus = metadataStatus,
      readStatus = readStatus
    )

    return seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageRequest)
      .map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return recently added or updated series.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("/latest")
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable
  ): Page<SeriesDto> {
    val sort = Sort.by(Sort.Order.desc("lastModified"))

    val pageRequest =
      if (unpaged) UnpagedSorted(sort)
      else PageRequest.of(
        page.pageNumber,
        page.pageSize,
        sort
      )

    return seriesDtoRepository.findAll(
      SeriesSearchWithReadProgress(principal.user.getAuthorizedLibraryIds(null)),
      principal.user.id,
      pageRequest
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return newly added series.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("/new")
  fun getNewSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable
  ): Page<SeriesDto> {
    val sort = Sort.by(Sort.Order.desc("created"))

    val pageRequest =
      if (unpaged) UnpagedSorted(sort)
      else PageRequest.of(
        page.pageNumber,
        page.pageSize,
        sort
      )

    return seriesDtoRepository.findAll(
      SeriesSearchWithReadProgress(principal.user.getAuthorizedLibraryIds(null)),
      principal.user.id,
      pageRequest
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return recently updated series, but not newly added ones.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("/updated")
  fun getUpdatedSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable
  ): Page<SeriesDto> {
    val sort = Sort.by(Sort.Order.desc("lastModified"))

    val pageRequest =
      if (unpaged) UnpagedSorted(sort)
      else PageRequest.of(
        page.pageNumber,
        page.pageSize,
        sort
      )

    return seriesDtoRepository.findRecentlyUpdated(
      SeriesSearchWithReadProgress(principal.user.getAuthorizedLibraryIds(null)),
      principal.user.id,
      pageRequest
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @GetMapping("{seriesId}")
  fun getOneSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") id: String
  ): SeriesDto =
    seriesDtoRepository.findByIdOrNull(id, principal.user.id)?.let {
      if (!principal.user.canAccessLibrary(it.libraryId)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      it.restrictUrl(!principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["{seriesId}/thumbnail"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getSeriesThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String
  ): ResponseEntity<ByteArray> {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return bookRepository.findFirstIdInSeries(seriesId)?.let {
      bookController.getBookThumbnail(principal, it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PageableAsQueryParam
  @GetMapping("{seriesId}/books")
  fun getAllBooksBySeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String,
    @RequestParam(name = "media_status", required = false) mediaStatus: List<Media.Status>?,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>?,
    @Parameter(hidden = true) page: Pageable
  ): Page<BookDto> {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      if (page.sort.isSorted) Sort.by(page.sort.map { it.ignoreCase() }.toList())
      else Sort.by(Sort.Order.asc("metadata.numberSort"))
    )

    return bookDtoRepository.findAll(
      BookSearchWithReadProgress(
        seriesIds = listOf(seriesId),
        mediaStatus = mediaStatus,
        readStatus = readStatus
      ),
      principal.user.id,
      pageRequest
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @GetMapping("{seriesId}/collections")
  fun getAllCollectionsBySeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String
  ): List<CollectionDto> {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return collectionRepository.findAllBySeries(seriesId, principal.user.getAuthorizedLibraryIds(null))
      .map { it.toDto() }
  }

  @PostMapping("{seriesId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable seriesId: String) {
    bookRepository.findAllIdBySeriesId(seriesId).forEach {
      taskReceiver.analyzeBook(it)
    }
  }

  @PostMapping("{seriesId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(@PathVariable seriesId: String) {
    bookRepository.findAllIdBySeriesId(seriesId).forEach {
      taskReceiver.refreshBookMetadata(it)
    }
  }

  @PatchMapping("{seriesId}/metadata")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateMetadata(
    @PathVariable seriesId: String,
    @Parameter(description = "Metadata fields to update. Set a field to null to unset the metadata. You can omit fields you don't want to update.")
    @Valid @RequestBody newMetadata: SeriesMetadataUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) =
    seriesMetadataRepository.findByIdOrNull(seriesId)?.let { existing ->
      val updated = with(newMetadata) {
        existing.copy(
          status = status ?: existing.status,
          statusLock = statusLock ?: existing.statusLock,
          title = title ?: existing.title,
          titleLock = titleLock ?: existing.titleLock,
          titleSort = titleSort ?: existing.titleSort,
          titleSortLock = titleSortLock ?: existing.titleSortLock
        )
      }
      seriesMetadataRepository.update(updated)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PostMapping("{seriesId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAsRead(
    @PathVariable seriesId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    bookRepository.findAllIdBySeriesId(seriesId).forEach {
      bookLifecycle.markReadProgressCompleted(it, principal.user)
    }
  }

  @DeleteMapping("{seriesId}/read-progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markAsUnread(
    @PathVariable seriesId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    bookRepository.findAllIdBySeriesId(seriesId).forEach {
      bookLifecycle.deleteReadProgress(it, principal.user)
    }
  }
}
