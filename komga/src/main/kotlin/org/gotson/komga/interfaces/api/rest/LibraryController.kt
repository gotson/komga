package org.gotson.komga.interfaces.api.rest

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
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
import org.gotson.komga.domain.service.SeriesLifecycle
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
import java.io.FileNotFoundException

@RestController
@RequestMapping("api/v1/libraries", produces = [MediaType.APPLICATION_JSON_VALUE])
class LibraryController(
  private val taskEmitter: TaskEmitter,
  private val libraryLifecycle: LibraryLifecycle,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val seriesRepository: SeriesRepository,
  private val seriesLifecycle: SeriesLifecycle,
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
    @Valid @RequestBody
    library: LibraryCreationDto,
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
          importComicInfoSeriesAppendVolume = library.importComicInfoSeriesAppendVolume,
          importEpubBook = library.importEpubBook,
          importEpubSeries = library.importEpubSeries,
          importMylarSeries = library.importMylarSeries,
          importLocalArtwork = library.importLocalArtwork,
          importBarcodeIsbn = library.importBarcodeIsbn,
          scanForceModifiedTime = library.scanForceModifiedTime,
          scanInterval = library.scanInterval.toDomain(),
          scanOnStartup = library.scanOnStartup,
          scanCbx = library.scanCbx,
          scanPdf = library.scanPdf,
          scanEpub = library.scanEpub,
          scanDirectoryExclusions = library.scanDirectoryExclusions,
          repairExtensions = library.repairExtensions,
          convertToCbz = library.convertToCbz,
          emptyTrashAfterScan = library.emptyTrashAfterScan,
          seriesCover = library.seriesCover.toDomain(),
          seriesSort = library.seriesSort.toDomain(),
          hashFiles = library.hashFiles,
          hashPages = library.hashPages,
          analyzeDimensions = library.analyzeDimensions,
          oneshotsDirectory = library.oneshotsDirectory?.ifBlank { null },
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
  @Deprecated("Use PATCH /v1/library instead", ReplaceWith("patchOne"))
  fun updateOne(
    @PathVariable libraryId: String,
    @Valid @RequestBody
    library: LibraryUpdateDto,
  ) {
    patchOne(libraryId, library)
  }

  @PatchMapping("/{libraryId}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun patchOne(
    @PathVariable libraryId: String,
    @Parameter(description = "Fields to update. You can omit fields you don't want to update.")
    @Valid
    @RequestBody
    library: LibraryUpdateDto,
  ) {
    libraryRepository.findByIdOrNull(libraryId)?.let { existing ->
      val toUpdate = with(library) {
        existing.copy(
          id = libraryId,
          name = name ?: existing.name,
          root = root?.let { filePathToUrl(root!!) } ?: existing.root,
          importComicInfoBook = importComicInfoBook ?: existing.importComicInfoBook,
          importComicInfoSeries = importComicInfoSeries ?: existing.importComicInfoSeries,
          importComicInfoCollection = importComicInfoCollection ?: existing.importComicInfoCollection,
          importComicInfoReadList = importComicInfoReadList ?: existing.importComicInfoReadList,
          importComicInfoSeriesAppendVolume = importComicInfoSeriesAppendVolume ?: existing.importComicInfoSeriesAppendVolume,
          importEpubBook = importEpubBook ?: existing.importEpubBook,
          importEpubSeries = importEpubSeries ?: existing.importEpubSeries,
          importMylarSeries = importMylarSeries ?: existing.importMylarSeries,
          importLocalArtwork = importLocalArtwork ?: existing.importLocalArtwork,
          importBarcodeIsbn = importBarcodeIsbn ?: existing.importBarcodeIsbn,
          scanForceModifiedTime = scanForceModifiedTime ?: existing.scanForceModifiedTime,
          scanInterval = scanInterval?.toDomain() ?: existing.scanInterval,
          scanOnStartup = scanOnStartup ?: existing.scanOnStartup,
          scanCbx = scanCbx ?: existing.scanCbx,
          scanPdf = scanPdf ?: existing.scanPdf,
          scanEpub = scanEpub ?: existing.scanEpub,
          scanDirectoryExclusions = if (isSet("scanDirectoryExclusions")) scanDirectoryExclusions ?: emptySet() else existing.scanDirectoryExclusions,
          repairExtensions = repairExtensions ?: existing.repairExtensions,
          convertToCbz = convertToCbz ?: existing.convertToCbz,
          emptyTrashAfterScan = emptyTrashAfterScan ?: existing.emptyTrashAfterScan,
          seriesCover = seriesCover?.toDomain() ?: existing.seriesCover,
          seriesSort = seriesSort?.toDomain() ?: existing.seriesSort,
          hashFiles = hashFiles ?: existing.hashFiles,
          hashPages = hashPages ?: existing.hashPages,
          analyzeDimensions = analyzeDimensions ?: existing.analyzeDimensions,
          oneshotsDirectory = if (isSet("oneshotsDirectory")) oneshotsDirectory?.ifBlank { null } else existing.oneshotsDirectory,
        )
      }
      try {
        libraryLifecycle.updateLibrary(toUpdate)
        if (toUpdate.seriesSort != existing.seriesSort) {
          seriesRepository.findAllByLibraryId(libraryId).forEach { seriesLifecycle.sortBooks(it) }
        }
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
  fun scan(
    @PathVariable libraryId: String,
    @RequestParam(required = false) deep: Boolean = false,
  ) {
    libraryRepository.findByIdOrNull(libraryId)?.let { library ->
      taskEmitter.scanLibrary(library.id, deep, HIGHEST_PRIORITY)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("{libraryId}/analyze")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun analyze(@PathVariable libraryId: String) {
    taskEmitter.analyzeBook(bookRepository.findAll(BookSearch(libraryIds = listOf(libraryId))), HIGH_PRIORITY)
  }

  @PostMapping("{libraryId}/metadata/refresh")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun refreshMetadata(@PathVariable libraryId: String) {
    val books = bookRepository.findAll(BookSearch(libraryIds = listOf(libraryId)))
    taskEmitter.refreshBookMetadata(books, priority = HIGH_PRIORITY)
    taskEmitter.refreshBookLocalArtwork(books, priority = HIGH_PRIORITY)
    taskEmitter.refreshSeriesLocalArtwork(seriesRepository.findAllIdsByLibraryId(libraryId), priority = HIGH_PRIORITY)
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
