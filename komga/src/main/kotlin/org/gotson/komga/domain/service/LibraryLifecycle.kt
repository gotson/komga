package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.scheduler.LibraryScanScheduler
import org.gotson.komga.application.tasks.LOWEST_PRIORITY
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.SidecarRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.io.FileNotFoundException
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

@Service
class LibraryLifecycle(
  private val libraryRepository: LibraryRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val seriesRepository: SeriesRepository,
  private val sidecarRepository: SidecarRepository,
  private val taskEmitter: TaskEmitter,
  private val eventPublisher: ApplicationEventPublisher,
  private val transactionTemplate: TransactionTemplate,
  private val libraryScanScheduler: LibraryScanScheduler,
) {
  @Throws(
    FileNotFoundException::class,
    DirectoryNotFoundException::class,
    DuplicateNameException::class,
    PathContainedInPath::class,
  )
  fun addLibrary(library: Library): Library {
    logger.info { "Adding new library: ${library.name} with root folder: ${library.root}" }

    val existing = libraryRepository.findAll()
    checkLibraryValidity(library, existing)

    libraryRepository.insert(library)
    taskEmitter.scanLibrary(library.id)

    eventPublisher.publishEvent(DomainEvent.LibraryAdded(library))

    return libraryRepository.findById(library.id)
  }

  fun updateLibrary(toUpdate: Library) {
    logger.info { "Updating library: ${toUpdate.id}" }

    val libraries = libraryRepository.findAll()
    val otherLibraries = libraries.filter { it.id != toUpdate.id }
    val current = libraries.first { it.id == toUpdate.id }
    checkLibraryValidity(toUpdate, otherLibraries)

    libraryRepository.update(toUpdate)

    if (current.scanInterval != toUpdate.scanInterval)
      libraryScanScheduler.scheduleScan(toUpdate)

    if (checkLibraryShouldRescan(current, toUpdate))
      taskEmitter.scanLibrary(toUpdate.id)

    if (toUpdate.hashFiles && !current.hashFiles)
      taskEmitter.hashBooksWithoutHash(toUpdate)
    if (toUpdate.hashKoreader && !current.hashKoreader)
      taskEmitter.hashBooksWithoutHashKoreader(toUpdate)
    if (toUpdate.hashPages && !current.hashPages)
      taskEmitter.findBooksWithMissingPageHash(toUpdate, LOWEST_PRIORITY)
    if (toUpdate.repairExtensions && !current.repairExtensions)
      taskEmitter.repairExtensions(toUpdate, LOWEST_PRIORITY)
    if (toUpdate.convertToCbz && !current.convertToCbz)
      taskEmitter.findBooksToConvert(toUpdate, LOWEST_PRIORITY)

    eventPublisher.publishEvent(DomainEvent.LibraryUpdated(toUpdate))
  }

  private fun checkLibraryShouldRescan(
    existing: Library,
    updated: Library,
  ): Boolean {
    if (existing.root != updated.root) return true
    if (existing.oneshotsDirectory != updated.oneshotsDirectory) return true
    if (existing.scanCbx != updated.scanCbx) return true
    if (existing.scanPdf != updated.scanPdf) return true
    if (existing.scanEpub != updated.scanEpub) return true
    if (existing.scanForceModifiedTime != updated.scanForceModifiedTime) return true
    if (existing.scanDirectoryExclusions != updated.scanDirectoryExclusions) return true
    return false
  }

  private fun checkLibraryValidity(
    library: Library,
    existing: Collection<Library>,
  ) {
    if (!Files.exists(library.path))
      throw FileNotFoundException("Library root folder does not exist: ${library.root}")

    if (!Files.isDirectory(library.path))
      throw DirectoryNotFoundException("Library root folder is not a folder: ${library.root}")

    if (existing.map { it.name }.contains(library.name))
      throw DuplicateNameException("Library name already exists")

    existing.forEach {
      if (library.path.startsWith(it.path))
        throw PathContainedInPath("Library path ${library.path} is a child of existing library ${it.name}: ${it.path}")
      if (it.path.startsWith(library.path))
        throw PathContainedInPath("Library path ${library.path} is a parent of existing library ${it.name}: ${it.path}")
    }
  }

  fun deleteLibrary(library: Library) {
    logger.info { "Deleting library: $library" }

    val series = seriesRepository.findAllByLibraryId(library.id)
    transactionTemplate.executeWithoutResult {
      seriesLifecycle.deleteMany(series)
      sidecarRepository.deleteByLibraryId(library.id)

      libraryRepository.delete(library.id)
    }

    eventPublisher.publishEvent(DomainEvent.LibraryDeleted(library))
  }
}
