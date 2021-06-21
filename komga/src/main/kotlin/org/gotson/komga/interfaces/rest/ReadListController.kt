package org.gotson.komga.interfaces.rest

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mu.KotlinLogging
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.ReadListLifecycle
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.language.toIndexedMap
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.swagger.PageableWithoutSortAsQueryParam
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.gotson.komga.interfaces.rest.dto.ReadListCreationDto
import org.gotson.komga.interfaces.rest.dto.ReadListDto
import org.gotson.komga.interfaces.rest.dto.ReadListRequestResultDto
import org.gotson.komga.interfaces.rest.dto.ReadListUpdateDto
import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressDto
import org.gotson.komga.interfaces.rest.dto.TachiyomiReadProgressUpdateDto
import org.gotson.komga.interfaces.rest.dto.restrictUrl
import org.gotson.komga.interfaces.rest.dto.toDto
import org.gotson.komga.interfaces.rest.persistence.BookDtoRepository
import org.gotson.komga.interfaces.rest.persistence.ReadProgressDtoRepository
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
import java.util.concurrent.TimeUnit
import javax.validation.Valid

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/readlists", produces = [MediaType.APPLICATION_JSON_VALUE])
class ReadListController(
  private val readListRepository: ReadListRepository,
  private val readListLifecycle: ReadListLifecycle,
  private val bookDtoRepository: BookDtoRepository,
  private val readProgressDtoRepository: ReadProgressDtoRepository,
  private val bookLifecycle: BookLifecycle,
) {

  @PageableWithoutSortAsQueryParam
  @GetMapping
  fun getAll(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String?,
    @RequestParam(name = "library_id", required = false) libraryIds: List<String>?,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable
  ): Page<ReadListDto> {
    val pageRequest =
      if (unpaged) UnpagedSorted(Sort.by(Sort.Order.asc("name")))
      else PageRequest.of(
        page.pageNumber,
        page.pageSize,
        Sort.by(Sort.Order.asc("name"))
      )

    return when {
      principal.user.sharedAllLibraries && libraryIds == null -> readListRepository.searchAll(
        searchTerm,
        pageable = pageRequest
      )
      principal.user.sharedAllLibraries && libraryIds != null -> readListRepository.findAllByLibraryIds(
        libraryIds,
        null,
        searchTerm,
        pageable = pageRequest
      )
      !principal.user.sharedAllLibraries && libraryIds != null -> readListRepository.findAllByLibraryIds(
        libraryIds,
        principal.user.sharedLibrariesIds,
        searchTerm,
        pageable = pageRequest
      )
      else -> readListRepository.findAllByLibraryIds(
        principal.user.sharedLibrariesIds,
        principal.user.sharedLibrariesIds,
        searchTerm,
        pageable = pageRequest
      )
    }.map { it.toDto() }
  }

  @GetMapping("{id}")
  fun getOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String
  ): ReadListDto =
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))
      ?.toDto()
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @ApiResponse(content = [Content(schema = Schema(type = "string", format = "binary"))])
  @GetMapping(value = ["{id}/thumbnail"], produces = [MediaType.IMAGE_JPEG_VALUE])
  fun getReadListThumbnail(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String
  ): ResponseEntity<ByteArray> {
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let {
      return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
        .body(readListLifecycle.getThumbnailBytes(it))
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun addOne(
    @Valid @RequestBody readList: ReadListCreationDto
  ): ReadListDto =
    try {
      readListLifecycle.addReadList(
        ReadList(
          name = readList.name,
          bookIds = readList.bookIds.toIndexedMap()
        )
      ).toDto()
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
    }

  @PostMapping("/import")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun importFromComicRackList(
    @RequestParam("files") files: List<MultipartFile>,
  ): List<ReadListRequestResultDto> =
    files.map { readListLifecycle.importReadList(it.bytes).toDto(it.originalFilename) }

  @PatchMapping("{id}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateOne(
    @PathVariable id: String,
    @Valid @RequestBody readList: ReadListUpdateDto
  ) {
    readListRepository.findByIdOrNull(id)?.let { existing ->
      val updated = existing.copy(
        name = readList.name ?: existing.name,
        bookIds = readList.bookIds?.toIndexedMap() ?: existing.bookIds
      )
      try {
        readListLifecycle.updateReadList(updated)
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
    readListRepository.findByIdOrNull(id)?.let {
      readListLifecycle.deleteReadList(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PageableWithoutSortAsQueryParam
  @GetMapping("{id}/books")
  fun getBooksForReadList(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable
  ): Page<BookDto> =
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { readList ->
      val sort = Sort.by(Sort.Order.asc("readList.number"))

      val pageRequest =
        if (unpaged) UnpagedSorted(sort)
        else PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort
        )

      bookDtoRepository.findAllByReadListId(
        readList.id,
        principal.user.id,
        principal.user.getAuthorizedLibraryIds(null),
        pageRequest
      )
        .map { it.restrictUrl(!principal.user.roleAdmin) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("{id}/books/{bookId}/previous")
  fun getBookSiblingPrevious(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @PathVariable bookId: String
  ): BookDto =
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let {
      bookDtoRepository.findPreviousInReadListOrNull(
        id,
        bookId,
        principal.user.id,
        principal.user.getAuthorizedLibraryIds(null)
      )
        ?.restrictUrl(!principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("{id}/books/{bookId}/next")
  fun getBookSiblingNext(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: String,
    @PathVariable bookId: String
  ): BookDto =
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let {
      bookDtoRepository.findNextInReadListOrNull(
        id,
        bookId,
        principal.user.id,
        principal.user.getAuthorizedLibraryIds(null)
      )
        ?.restrictUrl(!principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("{id}/read-progress/tachiyomi")
  fun getReadProgress(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ): TachiyomiReadProgressDto =
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { readList ->
      readProgressDtoRepository.findProgressByReadList(readList.id, principal.user.id)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PutMapping("{id}/read-progress/tachiyomi")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markReadProgressTachiyomi(
    @PathVariable id: String,
    @Valid @RequestBody readProgress: TachiyomiReadProgressUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) {
    readListRepository.findByIdOrNull(id, principal.user.getAuthorizedLibraryIds(null))?.let { readList ->
      bookDtoRepository.findAllByReadListId(
        readList.id,
        principal.user.id,
        principal.user.getAuthorizedLibraryIds(null),
        UnpagedSorted(Sort.by(Sort.Order.asc("readList.number")))
      ).filterIndexed { index, _ -> index < readProgress.lastBookRead }
        .forEach { book -> bookLifecycle.markReadProgressCompleted(book.id, principal.user) }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}
