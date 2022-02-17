package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.PageHash
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.persistence.PageHashRepository
import org.gotson.komga.domain.service.PageHashLifecycle
import org.gotson.komga.infrastructure.swagger.PageableAsQueryParam
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
import javax.validation.Valid

@RestController
@RequestMapping("api/v1/page-hashes", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('$ROLE_ADMIN')")
class PageHashController(
  private val pageHashRepository: PageHashRepository,
  private val pageHashLifecycle: PageHashLifecycle,
  private val taskEmitter: TaskEmitter,
) {

  @GetMapping
  @PageableAsQueryParam
  fun getKnownPageHashes(
    @RequestParam(name = "action", required = false) actions: List<PageHashKnown.Action>?,
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashKnownDto> =
    pageHashRepository.findAllKnown(actions, page).map { it.toDto() }

  @GetMapping("/{pageHash}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  fun getKnownPageHashThumbnail(
    @PathVariable pageHash: String,
    @RequestParam("media_type") mediaType: String,
    @RequestParam("file_size") size: Long,
  ): ByteArray =
    pageHashRepository.getKnownThumbnail(PageHash(pageHash, mediaType, size))
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("/unknown")
  @PageableAsQueryParam
  fun getUnknownPageHashes(
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashUnknownDto> =
    pageHashRepository.findAllUnknown(page).map { it.toDto() }

  @GetMapping("{pageHash}")
  @PageableAsQueryParam
  fun getPageHashMatches(
    @PathVariable pageHash: String,
    @RequestParam("media_type") mediaType: String,
    @RequestParam("file_size") size: Long,
    @Parameter(hidden = true) page: Pageable,
  ): Page<PageHashMatchDto> =
    pageHashRepository.findMatchesByHash(
      PageHash(pageHash, mediaType, size),
      null,
      page,
    ).map { it.toDto() }

  @GetMapping("unknown/{pageHash}/thumbnail", produces = [MediaType.IMAGE_JPEG_VALUE])
  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  fun getUnknownPageHashThumbnail(
    @PathVariable pageHash: String,
    @RequestParam("media_type") mediaType: String,
    @RequestParam("file_size") size: Long,
    @RequestParam("resize") resize: Int? = null,
  ): ResponseEntity<ByteArray> =
    pageHashLifecycle.getPage(
      PageHash(pageHash, mediaType, size),
      resize,
    )?.let {
      ResponseEntity.ok()
        .contentType(getMediaTypeOrDefault(it.mediaType))
        .body(it.content)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PutMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun createOrUpdateKnownPageHash(
    @Valid @RequestBody pageHash: PageHashCreationDto,
  ) {
    try {
      pageHashLifecycle.createOrUpdate(
        PageHashKnown(
          hash = pageHash.hash,
          mediaType = pageHash.mediaType,
          size = pageHash.size,
          action = pageHash.action,
        ),
      )
    } catch (e: IllegalArgumentException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
    }
  }

  @PostMapping("{pageHash}/delete-all")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun performDelete(
    @PathVariable pageHash: String,
    @RequestParam("media_type") mediaType: String,
    @RequestParam("file_size") size: Long,
  ) {
    val hash = PageHash(pageHash, mediaType, size)

    val toRemove = pageHashRepository.findMatchesByHash(hash, null, Pageable.unpaged())
      .groupBy(
        { it.bookId },
        {
          BookPageNumbered(
            fileName = it.fileName,
            mediaType = hash.mediaType,
            fileHash = hash.hash,
            fileSize = hash.size,
            pageNumber = it.pageNumber,
          )
        },
      )

    toRemove.forEach { taskEmitter.removeDuplicatePages(it.key, it.value) }
  }

  @PostMapping("{pageHash}/delete-match")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun deleteSingleMatch(
    @PathVariable pageHash: String,
    @RequestParam("media_type") mediaType: String,
    @RequestParam("file_size") size: Long,
    @RequestBody matchDto: PageHashMatchDto,
  ) {
    val toRemove = Pair(
      matchDto.bookId,
      listOf(
        BookPageNumbered(
          fileName = matchDto.fileName,
          mediaType = mediaType,
          fileHash = pageHash,
          fileSize = size,
          pageNumber = matchDto.pageNumber,
        ),
      ),
    )

    taskEmitter.removeDuplicatePages(toRemove.first, toRemove.second)
  }
}
