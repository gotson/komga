package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.application.tasks.HIGHEST_PRIORITY
import org.gotson.komga.application.tasks.HIGH_PRIORITY
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.LibraryLifecycle
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.web.filePathToUrl
import org.gotson.komga.interfaces.api.rest.dto.LibraryCreationDto
import org.gotson.komga.interfaces.api.rest.dto.LibraryDto
import org.gotson.komga.interfaces.api.rest.dto.LibraryUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.toDomain
import org.gotson.komga.interfaces.api.rest.dto.toDto
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
import java.io.FileNotFoundException
import javax.validation.Valid

@RestController
@RequestMapping("api/v1/libraries", produces = [MediaType.APPLICATION_JSON_VALUE])
class LibraryController(
  private val taskEmitter: TaskEmitter,
  private val libraryLifecycle: LibraryLifecycle,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val seriesRepository: SeriesRepository,
) {

  @GetMapping
  fun getAll(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): List<LibraryDto> =
    if (principal.user.sharedAllLibraries) {
      libraryRepository.findAll()
    } else {
      libraryRepository.findAllByIds(principal.user.sharedLibrariesIds)
    }.sortedBy { it.name.lowercase() }.map { it.toDto(includeRoot = principal.user.roleAdmin) }

  @GetMapping("{libraryId}")
  fun getOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable libraryId: String,
  ): LibraryDto =
    libraryRepository.findByIdOrNull(libraryId)?.let {
      if (!principal.user.canAccessLibrary(it)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
      it.toDto(includeRoot = principal.user.roleAdmin)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @PostMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun addOne(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Valid @RequestBody library: LibraryCreationDto,
  ): LibraryDto =
    try {
      libraryLifecycle.addLibrary(
        Library(
          name = library.name,
          root = filePathToUrl(library.root),
          importComicInfoBook = library.importComicInfoBook,
          importComicInfoSeries = library.importComicInfoSeries,
          importComicInfoCollection = library.importComicInfoCollection,
          importComicInfoReadList = library.importComicInfoReadList,
          importEpubBook = library.importEpubBook,
          importEpubSeries = library.importEpubSeries,
          importMylarSeries = library.importMylarSeries,
          importLocalArtwork = library.importLocalArtwork,
          importBarcodeIsbn = library.importBarcodeIsbn,
          scanForceModifiedTime = library.scanForceModifiedTime,
          scanDeep = library.scanDeep,
          repairExtensions = library.repairExtensions,
          convertToCbz = library.convertToCbz,
          emptyTrashAfterScan = library.emptyTrashAfterScan,
          seriesCover = library.seriesCover.toDomain(),
          hashFiles = library.hashFiles,
          hashPages = library.hashPages,
          analyzeDimensions = library.analyzeDimensions,
        ),
      ).toDto(includeRoot = principal.user.roleAdmin)
    } catch (e: Exception) {
      when (e) {
        is FileNotFoundException,
        is DirectoryNotFoundException,
        is DuplicateNameException,
        is PathContainedInPath,
        ->
          throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
      }
    }

  @PutMapping("/{libraryId}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateOne(
    @PathVariable libraryId: String,
    @Valid @RequestBody library: LibraryUpdateDto,
  ) {
    libraryRepository.findByIdOrNull(libraryId)?.let {
      val toUpdate = Library(
        id = libraryId,
        name = library.name,
        root = filePathToUrl(library.root),
        importComicInfoBook = library.importComicInfoBook,
        importComicInfoSeries = library.importComicInfoSeries,
        importComicInfoCollection = library.importComicInfoCollection,
        importComicInfoReadList = library.importComicInfoReadList,
        importEpubBook = library.importEpubBook,
        importEpubSeries = library.importEpubSeries,
        importMylarSeries = library.importMylarSeries,
        importLocalArtwork = library.importLocalArtwork,
        importBarcodeIsbn = library.importBarcodeIsbn,
        scanForceModifiedTime = library.scanForceModifiedTime,
        scanDeep = library.scanDeep,
        repairExtensions = library.repairExtensions,
        convertToCbz = library.convertToCbz,
        emptyTrashAfterScan = library.emptyTrashAfterScan,
        seriesCover = library.seriesCover.toDomain(),
        hashFiles = library.hashFiles,
        hashPages = library.hashPages,
        analyzeDimensions = library.analyzeDimensions,
      )
      try {
        libraryLifecycle.updateLibrary(toUpdate)
      } catch (e: Exception) {
        when (e) {
          is FileNotFoundException,
          is DirectoryNotFoundException,
          is DuplicateNameException,
          is PathContainedInPath,
          ->
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
          else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }
      }
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
      taskEmitter.scanLibrary(library.id, HIGHEST_PRIORITY)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("{libraryId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable libraryId: String) {
    bookRepository.findAll(BookSearch(libraryIds = listOf(libraryId))).forEach {
      taskEmitter.analyzeBook(it, HIGH_PRIORITY)
    }
  }

  @PostMapping("{libraryId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(@PathVariable libraryId: String) {
    bookRepository.findAll(BookSearch(libraryIds = listOf(libraryId))).forEach {
      taskEmitter.refreshBookMetadata(it, priority = HIGH_PRIORITY)
      taskEmitter.refreshBookLocalArtwork(it, priority = HIGH_PRIORITY)
    }
    seriesRepository.findAllIdsByLibraryId(libraryId).forEach {
      taskEmitter.refreshSeriesLocalArtwork(it, priority = HIGH_PRIORITY)
    }
  }

  @PostMapping("{libraryId}/empty-trash")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun emptyTrash(@PathVariable libraryId: String) {
    libraryRepository.findByIdOrNull(libraryId)?.let { library ->
      taskEmitter.emptyTrash(library.id, HIGH_PRIORITY)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}
