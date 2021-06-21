package org.gotson.komga.interfaces.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mu.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import org.gotson.komga.application.events.EventPublisher
import org.gotson.komga.application.tasks.HIGH_PRIORITY
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.Author
import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.SeriesSearchWithReadProgress
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.AuthorsAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
import org.gotson.komga.infrastructure.swagger.PageableWithoutSortAsQueryParam
import org.gotson.komga.infrastructure.web.Authors
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.gotson.komga.interfaces.rest.dto.CollectionDto
import org.gotson.komga.interfaces.rest.dto.SeriesDto
import org.gotson.komga.interfaces.rest.dto.SeriesMetadataUpdateDto
import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressDto
import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressUpdateDto
import org.gotson.komga.interfaces.rest.dto.restrictUrl
import org.gotson.komga.interfaces.rest.dto.toDto
import org.gotson.komga.interfaces.rest.persistence.BookDtoRepository
import org.gotson.komga.interfaces.rest.persistence.ReadProgressDtoRepository
import org.gotson.komga.interfaces.rest.persistence.SeriesDtoRepository
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
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream
import java.util.zip.Deflater
import javax.validation.Valid

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/series", produces = [MediaType.APPLICATION_JSON_VALUE])
class SeriesController(
  private val taskReceiver: TaskReceiver,
  private val seriesRepository: SeriesRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val seriesDtoRepository: SeriesDtoRepository,
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val readProgressDtoRepository: ReadProgressDtoRepository,
  private val eventPublisher: EventPublisher,
) {

  @PageableAsQueryParam
  @AuthorsAsQueryParam
  @GetMapping
  fun getAllSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "collection_id", required = false) collectionIds: List<String>?,
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
      readStatus = readStatus,
      publishers = publishers,
      languages = languages,
      genres = genres,
      tags = tags,
      ageRatings = ageRatings?.map { it.toIntOrNull() },
      releaseYears = release_years,
      authors = authors
    )

    return seriesDtoRepository.findAll(seriesSearch, principal.user.id, pageRequest)
      .map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return recently added or updated series.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("/latest")
  fun getLatestSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
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
      SeriesSearchWithReadProgress(principal.user.getAuthorizedLibraryIds(libraryIds)),
      principal.user.id,
      pageRequest
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return newly added series.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("/new")
  fun getNewSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
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
      SeriesSearchWithReadProgress(principal.user.getAuthorizedLibraryIds(libraryIds)),
      principal.user.id,
      pageRequest
    ).map { it.restrictUrl(!principal.user.roleAdmin) }
  }

  @Operation(description = "Return recently updated series, but not newly added ones.")
  @PageableWithoutSortAsQueryParam
  @GetMapping("/updated")
  fun getUpdatedSeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
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

    return seriesDtoRepository.findAllRecentlyUpdated(
      SeriesSearchWithReadProgress(principal.user.getAuthorizedLibraryIds(libraryIds)),
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
  ): ByteArray {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    return seriesLifecycle.getThumbnailBytes(seriesId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PageableAsQueryParam
  @AuthorsAsQueryParam
  @GetMapping("{seriesId}/books")
  fun getAllBooksBySeries(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable(name = "seriesId") seriesId: String,
    @RequestParam(name = "media_status", required = false) mediaStatus: List<Media.Status>?,
    @RequestParam(name = "read_status", required = false) readStatus: List<ReadStatus>?,
    @RequestParam(name = "tag", required = false) tags: List<String>?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) @Authors authors: List<Author>?,
    @Parameter(hidden = true) page: Pageable
  ): Page<BookDto> {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    val sort =
      if (page.sort.isSorted) page.sort
      else Sort.by(Sort.Order.asc("metadata.numberSort"))

    val pageRequest =
      if (unpaged) UnpagedSorted(sort)
      else PageRequest.of(
        page.pageNumber,
        page.pageSize,
        sort
      )

    return bookDtoRepository.findAll(
      BookSearchWithReadProgress(
        seriesIds = listOf(seriesId),
        mediaStatus = mediaStatus,
        readStatus = readStatus,
        tags = tags,
        authors = authors,
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

    return collectionRepository.findAllContainingSeriesId(seriesId, principal.user.getAuthorizedLibraryIds(null))
      .map { it.toDto() }
  }

  @PostMapping("{seriesId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable seriesId: String) {
    bookRepository.findAllIdsBySeriesId(seriesId).forEach {
      taskReceiver.analyzeBook(it, HIGH_PRIORITY)
    }
  }

  @PostMapping("{seriesId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(@PathVariable seriesId: String) {
    bookRepository.findAllIdsBySeriesId(seriesId).forEach {
      taskReceiver.refreshBookMetadata(it, priority = HIGH_PRIORITY)
      taskReceiver.refreshBookLocalArtwork(it, priority = HIGH_PRIORITY)
    }
    taskReceiver.refreshSeriesLocalArtwork(seriesId, priority = HIGH_PRIORITY)
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
          genres = if (isSet("genres")) {
            if (genres != null) genres!! else emptySet()
          } else existing.genres,
          genresLock = genresLock ?: existing.genresLock,
          tags = if (isSet("tags")) {
            if (tags != null) tags!! else emptySet()
          } else existing.tags,
          tagsLock = tagsLock ?: existing.tagsLock
        )
      }
      seriesMetadataRepository.update(updated)

      seriesRepository.findByIdOrNull(seriesId)?.let { eventPublisher.publishEvent(DomainEvent.SeriesUpdated(it)) }
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

    seriesLifecycle.markReadProgressCompleted(seriesId, principal.user)
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

    seriesLifecycle.deleteReadProgress(seriesId, principal.user)
  }

  @GetMapping("{seriesId}/read-progress/tachiyomi")
  fun getReadProgressTachiyomi(
    @PathVariable seriesId: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): TachiyomiReadProgressDto =
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      return readProgressDtoRepository.findProgressBySeries(seriesId, principal.user.id)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PutMapping("{seriesId}/read-progress/tachiyomi")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markReadProgressTachiyomi(
    @PathVariable seriesId: String,
    @Valid @RequestBody readProgress: TachiyomiReadProgressUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    bookDtoRepository.findAll(
      BookSearchWithReadProgress(seriesIds = listOf(seriesId)),
      principal.user.id,
      UnpagedSorted(Sort.by(Sort.Order.asc("metadata.numberSort"))),
    ).filterIndexed { index, _ -> index < readProgress.lastBookRead }
      .forEach { book ->
        if (book.readProgress?.completed != true)
          bookLifecycle.markReadProgressCompleted(book.id, principal.user)
      }
  }

  @GetMapping("{seriesId}/file", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  @PreAuthorize("hasRole('$ROLE_FILE_DOWNLOAD')")
  fun getSeriesFile(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable seriesId: String
  ): ResponseEntity<StreamingResponseBody> {
    seriesRepository.getLibraryId(seriesId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val books = bookRepository.findAllBySeriesId(seriesId)

    val streamingResponse = StreamingResponseBody { responseStream: OutputStream ->
      ZipArchiveOutputStream(responseStream).use { zipStream ->
        zipStream.setMethod(ZipArchiveOutputStream.DEFLATED)
        zipStream.setLevel(Deflater.NO_COMPRESSION)
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
          contentDisposition = ContentDisposition.builder("attachment")
            .filename(seriesMetadataRepository.findById(seriesId).title + ".zip")
            .build()
        }
      )
      .contentType(MediaType.parseMediaType("application/zip"))
      .body(streamingResponse)
  }
}
