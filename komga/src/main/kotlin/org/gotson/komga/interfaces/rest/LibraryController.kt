package org.gotson.komga.interfaces.rest

import mu.KotlinLogging
import org.gotson.komga.application.service.AsyncOrchestrator
import org.gotson.komga.application.service.LibraryLifecycle
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.FileNotFoundException
import java.util.concurrent.RejectedExecutionException
import javax.validation.Valid
import javax.validation.constraints.NotBlank

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/libraries", produces = [MediaType.APPLICATION_JSON_VALUE])
class LibraryController(
  private val libraryLifecycle: LibraryLifecycle,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val asyncOrchestrator: AsyncOrchestrator
) {

  @GetMapping
  fun getAll(
      @AuthenticationPrincipal principal: KomgaPrincipal
  ): List<LibraryDto> =
      if (principal.user.sharedAllLibraries) {
        libraryRepository.findAll(Sort.by("name"))
      } else {
        principal.user.sharedLibraries
      }.map { it.toDto(includeRoot = principal.user.isAdmin()) }

  @GetMapping("{id}")
  fun getOne(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @PathVariable id: Long
  ): LibraryDto =
      libraryRepository.findByIdOrNull(id)?.let {
        if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        it.toDto(includeRoot = principal.user.isAdmin())
      } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  fun addOne(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @Valid @RequestBody library: LibraryCreationDto
  ): LibraryDto =
      try {
        libraryLifecycle.addLibrary(Library(library.name, library.root)).toDto(includeRoot = principal.user.isAdmin())
      } catch (e: Exception) {
        when (e) {
          is FileNotFoundException,
          is DirectoryNotFoundException,
          is DuplicateNameException,
          is PathContainedInPath ->
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
          else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }
      }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteOne(@PathVariable id: Long) {
    libraryRepository.findByIdOrNull(id)?.let {
      libraryLifecycle.deleteLibrary(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("{libraryId}/scan")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun scan(@PathVariable libraryId: Long) {
    libraryRepository.findByIdOrNull(libraryId)?.let { library ->
      try {
        asyncOrchestrator.scanAndAnalyzeOneLibrary(library)
      } catch (e: RejectedExecutionException) {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Another scan task is already running")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("{libraryId}/analyze")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable libraryId: Long) {
    libraryRepository.findByIdOrNull(libraryId)?.let { library ->
      try {
        asyncOrchestrator.reAnalyzeBooks(bookRepository.findBySeriesLibraryIn(listOf(library)))
      } catch (e: RejectedExecutionException) {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Another book analysis task is already running")
      }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}

data class LibraryCreationDto(
    @get:NotBlank val name: String,
    @get:NotBlank val root: String
)

data class LibraryDto(
    val id: Long,
    val name: String,
    val root: String
)

fun Library.toDto(includeRoot: Boolean) = LibraryDto(
    id = id,
    name = name,
    root = if (includeRoot) root.toURI().path else ""
)
