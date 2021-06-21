package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.events.EventPublisher
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.CodedException
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.withCode
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.infrastructure.language.toIndexedMap
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.copyTo
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.moveTo
import kotlin.io.path.notExists

private val logger = KotlinLogging.logger {}

@Service
class BookImporter(
  private val bookLifecycle: BookLifecycle,
  private val fileSystemScanner: FileSystemScanner,
  private val seriesLifecycle: SeriesLifecycle,
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val metadataRepository: BookMetadataRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val readListRepository: ReadListRepository,
  private val libraryRepository: LibraryRepository,
  private val eventPublisher: EventPublisher,
) {

  fun importBook(sourceFile: Path, series: Series, copyMode: CopyMode, destinationName: String? = null, upgradeBookId: String? = null): Book {
    try {
      if (sourceFile.notExists()) throw FileNotFoundException("File not found: $sourceFile").withCode("ERR_1018")

      libraryRepository.findAll().forEach { library ->
        if (sourceFile.startsWith(library.path)) throw PathContainedInPath("Cannot import file that is part of an existing library", "ERR_1019")
      }

      val destFile = series.path.resolve(
        if (destinationName != null) Paths.get("$destinationName.${sourceFile.extension}").fileName.toString()
        else sourceFile.fileName.toString()
      )

      val upgradedBook =
        if (upgradeBookId != null) {
          bookRepository.findByIdOrNull(upgradeBookId)?.let {
            if (it.seriesId != series.id) throw IllegalArgumentException("Book to upgrade ($upgradeBookId) does not belong to series: $series").withCode("ERR_1020")
            it
          }
        } else null
      val upgradedBookPath =
        if (upgradedBook != null)
          bookRepository.findByIdOrNull(upgradedBook.id)?.path
        else null

      var deletedUpgradedFile = false
      when {
        upgradedBookPath != null && destFile == upgradedBookPath -> {
          logger.info { "Deleting existing file: $upgradedBookPath" }
          try {
            upgradedBookPath.deleteExisting()
            deletedUpgradedFile = true
          } catch (e: NoSuchFileException) {
            logger.warn { "Could not delete upgraded book: $upgradedBookPath" }
          }
        }
        destFile.exists() -> throw FileAlreadyExistsException("Destination file already exists: $destFile").withCode("ERR_1021")
      }

      when (copyMode) {
        CopyMode.MOVE -> {
          logger.info { "Moving file $sourceFile to $destFile" }
          sourceFile.moveTo(destFile)
        }
        CopyMode.COPY -> {
          logger.info { "Copying file $sourceFile to $destFile" }
          sourceFile.copyTo(destFile)
        }
        CopyMode.HARDLINK -> try {
          logger.info { "Hardlink file $sourceFile to $destFile" }
          Files.createLink(destFile, sourceFile)
        } catch (e: Exception) {
          logger.warn(e) { "Filesystem does not support hardlinks, copying instead" }
          sourceFile.copyTo(destFile)
        }
      }

      val importedBook = fileSystemScanner.scanFile(destFile)
        ?.copy(libraryId = series.libraryId)
        ?: throw IllegalStateException("Newly imported book could not be scanned: $destFile").withCode("ERR_1022")

      seriesLifecycle.addBooks(series, listOf(importedBook))

      if (upgradedBook != null) {
        // copy media and mark it as outdated
        mediaRepository.findById(upgradedBook.id).let {
          mediaRepository.update(
            it.copy(
              bookId = importedBook.id,
              status = Media.Status.OUTDATED,
            )
          )
        }

        // copy metadata
        metadataRepository.findById(upgradedBook.id).let {
          metadataRepository.update(it.copy(bookId = importedBook.id))
        }

        // copy read progress
        readProgressRepository.findAllByBookId(upgradedBook.id)
          .map { it.copy(bookId = importedBook.id) }
          .forEach { readProgressRepository.save(it) }

        // replace upgraded book by imported book in read lists
        readListRepository.findAllContainingBookId(upgradedBook.id, filterOnLibraryIds = null)
          .forEach { rl ->
            readListRepository.update(
              rl.copy(
                bookIds = rl.bookIds.values.map { if (it == upgradedBook.id) importedBook.id else it }.toIndexedMap()
              )
            )
          }

        // delete upgraded book file on disk if it has not been replaced earlier
        if (upgradedBookPath != null && !deletedUpgradedFile && upgradedBookPath.deleteIfExists())
          logger.info { "Deleted existing file: $upgradedBookPath" }

        // delete upgraded book
        bookLifecycle.deleteOne(upgradedBook)
      }

      seriesLifecycle.sortBooks(series)

      eventPublisher.publishEvent(DomainEvent.BookImported(importedBook, sourceFile.toUri().toURL(), success = true))

      return importedBook
    } catch (e: Exception) {
      val msg = if (e is CodedException) e.code else e.message
      eventPublisher.publishEvent(DomainEvent.BookImported(null, sourceFile.toUri().toURL(), success = false, msg))
      throw e
    }
  }
}
