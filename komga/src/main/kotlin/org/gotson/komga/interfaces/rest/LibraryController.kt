package org.gotson.komga.interfaces.rest

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.infrastructure.security.KomgaPrincipal
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
import javax.validation.Valid
import javax.validation.constraints.NotBlank

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/libraries", produces = [MediaType.APPLICATION_JSON_VALUE])
class LibraryController(
  private val taskReceiver: TaskReceiver,
  private val libraryLifecycle: LibraryLifecycle,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository
) {

  @GetMapping
  fun getAll(
    @AuthenticationPrincipal principal: KomgaPrincipal
  ): List<LibraryDto> =
    if (principal.user.sharedAllLibraries) {
      libraryRepository.findAll()
    } else {
      libraryRepository.findAllById(principal.user.sharedLibrariesIds)
    }.sortedBy { it.name }.map { it.toDto(includeRoot = principal.user.roleAdmin) }

  @GetMapping("{id}")
  fun getOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable id: Long
  ): LibraryDto =
    libraryRepository.findByIdOrNull(id)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
      it.toDto(includeRoot = principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  fun addOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Valid @RequestBody library: LibraryCreationDto
  ): LibraryDto =
    try {
      libraryLifecycle.addLibrary(Library(library.name, library.root)).toDto(includeRoot = principal.user.roleAdmin)
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
      taskReceiver.scanLibrary(library.id)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("{libraryId}/analyze")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable libraryId: Long) {
    bookRepository.findAllIdByLibraryId(libraryId).forEach {
      taskReceiver.analyzeBook(it)
    }
  }

  @PostMapping("{libraryId}/metadata/refresh")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(@PathVariable libraryId: Long) {
    bookRepository.findAllIdByLibraryId(libraryId).forEach {
      taskReceiver.refreshBookMetadata(it)
    }
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
