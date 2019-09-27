package org.gotson.komga.interfaces.web.rest

import mu.KotlinLogging
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.LibraryLifecycle
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
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
    private val libraryLifecycle: LibraryLifecycle,
    private val libraryRepository: LibraryRepository
) {

  @GetMapping
  fun getAll() =
      libraryRepository.findAll().map { it.toDto() }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  fun addOne(
      @Valid @RequestBody library: LibraryCreationDto
  ): LibraryDto =
      try {
        libraryLifecycle.addLibrary(Library(library.name, library.root)).toDto()
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

fun Library.toDto() = LibraryDto(
    id = id,
    name = name,
    root = root.toString()
)