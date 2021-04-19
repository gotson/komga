package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.persistence.BookRepository
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
  private val readProgressRepository: ReadProgressRepository,
  private val readListRepository: ReadListRepository,
  private val taskReceiver: TaskReceiver,
) {

  fun importBook(sourceFile: Path, series: Series, copyMode: CopyMode, destinationName: String? = null, upgradeBookId: String? = null) {
    if (sourceFile.notExists()) throw FileNotFoundException("File not found: $sourceFile")

    val destFile = series.path().resolve(
      if (destinationName != null) Paths.get("$destinationName.${sourceFile.extension}").fileName.toString()
      else sourceFile.fileName.toString()
    )

    val upgradedBookId =
      if (upgradeBookId != null) {
        bookRepository.findByIdOrNull(upgradeBookId)?.let {
          if (it.seriesId != series.id) throw IllegalArgumentException("Book to upgrade ($upgradeBookId) does not belong to series: $series")
          it.id
        }
      } else null
    val upgradedBookPath =
      if (upgradedBookId != null)
        bookRepository.findByIdOrNull(upgradedBookId)?.path()
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
      destFile.exists() -> throw FileAlreadyExistsException("Destination file already exists: $destFile")
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
      } catch (e: UnsupportedOperationException) {
        logger.warn { "Filesystem does not support hardlinks, copying instead" }
        sourceFile.copyTo(destFile)
      }
    }

    val importedBook = fileSystemScanner.scanFile(destFile)
      ?.copy(libraryId = series.libraryId)
      ?: throw IllegalStateException("Newly imported book could not be scanned: $destFile")

    seriesLifecycle.addBooks(series, listOf(importedBook))

    if (upgradedBookId != null) {
      // copy media and mark it as outdated
      mediaRepository.findById(upgradedBookId).let {
        mediaRepository.update(
          it.copy(
            bookId = importedBook.id,
            status = Media.Status.OUTDATED,
          )
        )
      }

      // copy read progress
      readProgressRepository.findByBookId(upgradedBookId)
        .map { it.copy(bookId = importedBook.id) }
        .forEach { readProgressRepository.save(it) }

      // replace upgraded book by imported book in read lists
      readListRepository.findAllByBook(upgradedBookId, filterOnLibraryIds = null)
        .forEach { rl ->
          readListRepository.update(
            rl.copy(
              bookIds = rl.bookIds.values.map { if (it == upgradedBookId) importedBook.id else it }.toIndexedMap()
            )
          )
        }

      // delete upgraded book file on disk if it has not been replaced earlier
      if (upgradedBookPath != null && !deletedUpgradedFile && upgradedBookPath.deleteIfExists())
        logger.info { "Deleted existing file: $upgradedBookPath" }

      // delete upgraded book
      bookLifecycle.deleteOne(upgradedBookId)
    }

    seriesLifecycle.sortBooks(series)

    taskReceiver.analyzeBook(importedBook)
  }
}
