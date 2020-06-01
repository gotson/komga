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

    if (!Files.exists(library.path()))
      throw FileNotFoundException("Library root folder does not exist: ${library.root}")

    if (!Files.isDirectory(library.path()))
      throw DirectoryNotFoundException("Library root folder is not a folder: ${library.root}")

    if (libraryRepository.existsByName(library.name))
      throw DuplicateNameException("Library name already exists")

    libraryRepository.findAll().forEach {
      if (library.path().startsWith(it.path()))
        throw PathContainedInPath("Library path ${library.path()} is a child of existing library ${it.name}: ${it.path()}")
      if (it.path().startsWith(library.path()))
        throw PathContainedInPath("Library path ${library.path()} is a parent of existing library ${it.name}: ${it.path()}")
    }

    return libraryRepository.insert(library).let {
      taskReceiver.scanLibrary(it.id)
      it
    }
  }

  fun deleteLibrary(library: Library) {
    logger.info { "Deleting library: $library" }

    seriesRepository.findByLibraryId(library.id).forEach {
      seriesLifecycle.deleteSeries(it.id)
    }

    libraryRepository.delete(library.id)
  }
}
