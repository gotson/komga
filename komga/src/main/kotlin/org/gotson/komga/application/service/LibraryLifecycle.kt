package org.gotson.komga.application.service

import mu.KotlinLogging
import org.gotson.komga.domain.model.DirectoryNotFoundException
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.concurrent.RejectedExecutionException

private val logger = KotlinLogging.logger {}

@Service
class LibraryLifecycle(
    private val libraryRepository: LibraryRepository,
    private val seriesRepository: SeriesRepository,
    private val userRepository: KomgaUserRepository,
    private val asyncOrchestrator: AsyncOrchestrator
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


    libraryRepository.save(library)

    logger.info { "Trying to launch a scan for the newly added library: ${library.name}" }
    try {
      asyncOrchestrator.scanAndAnalyzeAllLibraries()
    } catch (e: RejectedExecutionException) {
      logger.warn { "Another scan is already running, skipping" }
    }

    return library
  }

  @Transactional
  fun deleteLibrary(library: Library) {
    logger.info { "Deleting library: ${library.name} with root folder: ${library.root}" }

    logger.info { "Delete all series for this library" }
    seriesRepository.deleteByLibraryId(library.id)

    logger.info { "Remove shared library access for all users" }
    userRepository.findBySharedLibrariesContaining(library).let { users ->
      users.forEach { user -> user.sharedLibraries.removeIf { it.id == library.id } }
      userRepository.saveAll(users)
    }

    libraryRepository.delete(library)
  }
}
