package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.domain.service.PageHashLifecycle
import org.gotson.komga.infrastructure.openapi.OpenApiConfiguration
import org.gotson.komga.infrastructure.openapi.PageableAsQueryParam
import org.gotson.komga.infrastructure.web.getMediaTypeOrDefault
import org.gotson.komga.interfaces.api.rest.dto.PageHashCreationDto
import org.gotson.komga.interfaces.api.rest.dto.PageHashKnownDto
import org.gotson.komga.interfaces.api.rest.dto.PageHashMatchDto
import org.gotson.komga.interfaces.api.rest.dto.PageHashUnknownDto
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api/v1/page-hashes", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = OpenApiConfiguration.TagNames.DUPLICATE_PAGES)
class PageHashController(
  private val pageHashRepository: PageHashRepository,
  private val pageHashLifecycle: PageHashLifecycle,
  private val taskEmitter: TaskEmitter,
) {
  @Operation(summary = "List known duplicates")
  @GetMapping
  @PageableAsQueryParam
  fun getKnownPageHashes(
    @RequestParam(name = "action", required = false) actions: List<PageHashKnown.Action>?,
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashKnownDto> = pageHashRepository.findAllKnown(actions, page).map { it.toDto() }

  @Operation(summary = "Get known duplicate image thumbnail")
  @GetMapping("/{pageHash}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  fun getKnownPageHashThumbnail(
    @PathVariable pageHash: String,
  ): ByteArray =
    pageHashRepository.getKnownThumbnail(pageHash)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @Operation(summary = "List unknown duplicates")
  @GetMapping("/unknown")
  @PageableAsQueryParam
  fun getUnknownPageHashes(
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashUnknownDto> = pageHashRepository.findAllUnknown(page).map { it.toDto() }

  @Operation(summary = "List duplicate matches")
  @GetMapping("{pageHash}")
  @PageableAsQueryParam
  fun getPageHashMatches(
    @PathVariable pageHash: String,
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashMatchDto> =
    pageHashRepository
      .findMatchesByHash(
        pageHash,
        page,
      ).map { it.toDto() }

  @Operation(summary = "Get unknown duplicate image thumbnail")
  @GetMapping("unknown/{pageHash}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  fun getUnknownPageHashThumbnail(
    @PathVariable pageHash: String,
    @RequestParam("resize") resize: Int? = null,
  ): ResponseEntity<ByteArray> =
    pageHashLifecycle.getPage(pageHash, resize)?.let {
      ResponseEntity
        .ok()
        .contentType(getMediaTypeOrDefault(it.mediaType))
        .body(it.bytes)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @Operation(summary = "Mark duplicate page as known")
  @PutMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun createOrUpdateKnownPageHash(
    @Valid @RequestBody
    pageHash: PageHashCreationDto,
  ) {
    try {
      pageHashLifecycle.createOrUpdate(
        PageHashKnown(
          hash = pageHash.hash,
          size = pageHash.size,
          action = pageHash.action,
        ),
      )
    } catch (e: IllegalArgumentException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
    }
  }

  @Operation(summary = "Delete all duplicate pages by hash")
  @PostMapping("{pageHash}/delete-all")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteDuplicatePagesByPageHash(
    @PathVariable pageHash: String,
  ) {
    val toRemove =
      pageHashRepository
        .findMatchesByHash(pageHash, Pageable.unpaged())
        .groupBy(
          { it.bookId },
          {
            BookPageNumbered(
              fileName = it.fileName,
              mediaType = it.mediaType,
              fileHash = pageHash,
              fileSize = it.fileSize,
              pageNumber = it.pageNumber,
            )
          },
        )

    taskEmitter.removeDuplicatePages(toRemove)
  }

  @Operation(summary = "Delete specific duplicate page")
  @PostMapping("{pageHash}/delete-match")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteSingleMatchByPageHash(
    @PathVariable pageHash: String,
    @RequestBody matchDto: PageHashMatchDto,
  ) {
    val toRemove =
      Pair(
        matchDto.bookId,
        listOf(
          BookPageNumbered(
            fileName = matchDto.fileName,
            mediaType = matchDto.mediaType,
            fileHash = pageHash,
            fileSize = matchDto.fileSize,
            pageNumber = matchDto.pageNumber,
          ),
        ),
      )

    taskEmitter.removeDuplicatePages(toRemove.first, toRemove.second)
  }
}
