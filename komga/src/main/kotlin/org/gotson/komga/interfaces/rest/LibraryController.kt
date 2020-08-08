package org.gotson.komga.interfaces.rest

import filePathToUrl
import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.model.ROLE_ADMIN
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import toFilePath
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
    }.sortedBy { it.name.toLowerCase() }.map { it.toDto(includeRoot = principal.user.roleAdmin) }

  @GetMapping("{libraryId}")
  fun getOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable libraryId: String
  ): LibraryDto =
    libraryRepository.findByIdOrNull(libraryId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      it.toDto(includeRoot = principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PostMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun addOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Valid @RequestBody library: LibraryCreationDto
  ): LibraryDto =
    try {
      libraryLifecycle.addLibrary(
        Library(
          name = library.name,
          root = filePathToUrl(library.root),
          importComicInfoBook = library.importComicInfoBook,
          importComicInfoSeries = library.importComicInfoSeries,
          importComicInfoCollection = library.importComicInfoCollection,
          importEpubBook = library.importEpubBook,
          importEpubSeries = library.importEpubSeries
        )
      ).toDto(includeRoot = principal.user.roleAdmin)
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

  @PutMapping("/{libraryId}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateOne(
    @PathVariable libraryId: String,
    @Valid @RequestBody library: LibraryUpdateDto
  ) {
    libraryRepository.findByIdOrNull(libraryId)?.let {
      val toUpdate = Library(
        id = libraryId,
        name = library.name,
        root = filePathToUrl(library.root),
        importComicInfoBook = library.importComicInfoBook,
        importComicInfoSeries = library.importComicInfoSeries,
        importComicInfoCollection = library.importComicInfoCollection,
        importEpubBook = library.importEpubBook,
        importEpubSeries = library.importEpubSeries
      )
      libraryLifecycle.updateLibrary(toUpdate)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @DeleteMapping("/{libraryId}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteOne(@PathVariable libraryId: String) {
    libraryRepository.findByIdOrNull(libraryId)?.let {
      libraryLifecycle.deleteLibrary(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("{libraryId}/scan")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun scan(@PathVariable libraryId: String) {
    libraryRepository.findByIdOrNull(libraryId)?.let { library ->
      taskReceiver.scanLibrary(library.id)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("{libraryId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable libraryId: String) {
    bookRepository.findAllIdByLibraryId(libraryId).forEach {
      taskReceiver.analyzeBook(it)
    }
  }

  @PostMapping("{libraryId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(@PathVariable libraryId: String) {
    bookRepository.findAllIdByLibraryId(libraryId).forEach {
      taskReceiver.refreshBookMetadata(it)
    }
  }
}

data class LibraryCreationDto(
  @get:NotBlank val name: String,
  @get:NotBlank val root: String,
  val importComicInfoBook: Boolean = true,
  val importComicInfoSeries: Boolean = true,
  val importComicInfoCollection: Boolean = true,
  val importEpubBook: Boolean = true,
  val importEpubSeries: Boolean = true
)

data class LibraryDto(
  val id: String,
  val name: String,
  val root: String,
  val importComicInfoBook: Boolean,
  val importComicInfoSeries: Boolean,
  val importComicInfoCollection: Boolean,
  val importEpubBook: Boolean,
  val importEpubSeries: Boolean
)

data class LibraryUpdateDto(
  @get:NotBlank val name: String,
  @get:NotBlank val root: String,
  val importComicInfoBook: Boolean,
  val importComicInfoSeries: Boolean,
  val importComicInfoCollection: Boolean,
  val importEpubBook: Boolean,
  val importEpubSeries: Boolean
)

fun Library.toDto(includeRoot: Boolean) = LibraryDto(
  id = id,
  name = name,
  root = if (includeRoot) this.root.toFilePath() else "",
  importComicInfoBook = importComicInfoBook,
  importComicInfoSeries = importComicInfoSeries,
  importComicInfoCollection = importComicInfoCollection,
  importEpubBook = importEpubBook,
  importEpubSeries = importEpubSeries
)
