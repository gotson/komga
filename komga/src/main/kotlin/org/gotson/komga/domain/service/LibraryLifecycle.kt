package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

@Service
class LibraryLifecycle(
  private val libraryRepository: LibraryRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val seriesRepository: SeriesRepository,
  private val taskReceiver: TaskReceiver
) {

  @Throws(
    FileNotFoundException::class,
    DirectoryNotFoundException::class,
    DuplicateNameException::class,
    PathContainedInPath::class
  )
  fun addLibrary(library: Library): Library {
    logger.info { "Adding new library: ${library.name} with root folder: ${library.root}" }

    val existing = libraryRepository.findAll()
    checkLibraryValidity(library, existing)

    libraryRepository.insert(library)
    taskReceiver.scanLibrary(library.id)

    return libraryRepository.findById(library.id)
  }

  fun updateLibrary(toUpdate: Library) {
    logger.info { "Updating library: ${toUpdate.id}" }

    val existing = libraryRepository.findAll().filter { it.id != toUpdate.id }
    checkLibraryValidity(toUpdate, existing)

    libraryRepository.update(toUpdate)
    taskReceiver.scanLibrary(toUpdate.id)
  }

  private fun checkLibraryValidity(library: Library, existing: Collection<Library>) {
    if (!Files.exists(library.path()))
      throw FileNotFoundException("Library root folder does not exist: ${library.root}")

    if (!Files.isDirectory(library.path()))
      throw DirectoryNotFoundException("Library root folder is not a folder: ${library.root}")

    if (existing.map { it.name }.contains(library.name))
      throw DuplicateNameException("Library name already exists")

    existing.forEach {
      if (library.path().startsWith(it.path()))
        throw PathContainedInPath("Library path ${library.path()} is a child of existing library ${it.name}: ${it.path()}")
      if (it.path().startsWith(library.path()))
        throw PathContainedInPath("Library path ${library.path()} is a parent of existing library ${it.name}: ${it.path()}")
    }
  }

  fun deleteLibrary(library: Library) {
    logger.info { "Deleting library: $library" }

    val seriesIds = seriesRepository.findByLibraryId(library.id).map { it.id }
    seriesLifecycle.deleteMany(seriesIds)

    libraryRepository.delete(library.id)
  }
}
