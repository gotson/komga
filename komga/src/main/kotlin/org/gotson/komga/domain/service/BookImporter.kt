package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.application.tasks.TaskEmitter
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.CodedException
import org.gotson.komga.domain.model.CopyMode
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.HistoricalEvent
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.PathContainedInPath
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.model.ThumbnailBook
import org.gotson.komga.domain.model.withCode
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.HistoricalEventRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.ReadListRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.SidecarRepository
import org.gotson.komga.domain.persistence.ThumbnailBookRepository
import org.gotson.komga.language.toIndexedMap
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.copyTo
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.readAttributes
import kotlin.io.path.toPath

private val logger = KotlinLogging.logger {}

@Service
class BookImporter(
  private val bookLifecycle: BookLifecycle,
  private val fileSystemScanner: FileSystemScanner,
  private val seriesLifecycle: SeriesLifecycle,
  private val bookRepository: BookRepository,
  private val mediaRepository: MediaRepository,
  private val metadataRepository: BookMetadataRepository,
  private val thumbnailBookRepository: ThumbnailBookRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val readListRepository: ReadListRepository,
  private val libraryRepository: LibraryRepository,
  private val sidecarRepository: SidecarRepository,
  private val eventPublisher: ApplicationEventPublisher,
  private val taskEmitter: TaskEmitter,
  private val historicalEventRepository: HistoricalEventRepository,
  private val seriesRepository: SeriesRepository,
) {
  fun importBook(
    sourceFile: Path,
    series: Series,
    copyMode: CopyMode,
    destinationName: String? = null,
    upgradeBookId: String? = null,
  ): Book {
    try {
      if (sourceFile.notExists()) throw FileNotFoundException("File not found: $sourceFile").withCode("ERR_1018")
      if (series.oneshot && upgradeBookId.isNullOrEmpty()) throw IllegalArgumentException("Destination series is oneshot but upgradeBookId is missing")

      libraryRepository.findAll().forEach { library ->
        if (sourceFile.startsWith(library.path)) throw PathContainedInPath("Cannot import file that is part of an existing library", "ERR_1019")
      }

      val bookToUpgrade =
        if (upgradeBookId != null) {
          bookRepository.findByIdOrNull(upgradeBookId)?.also {
            if (it.seriesId != series.id) throw IllegalArgumentException("Book to upgrade ($upgradeBookId) does not belong to series: $series").withCode("ERR_1020")
          }
        } else {
          null
        }

      val destDir =
        if (series.oneshot)
          series.path.parent
        else
          series.path

      val destFile =
        destDir.resolve(
          if (destinationName != null)
            Paths.get("$destinationName.${sourceFile.extension}").name
          else
            sourceFile.name,
        )
      val sidecars =
        fileSystemScanner.scanBookSidecars(sourceFile).associateWith {
          destDir.resolve(
            if (destinationName != null)
              it.url
                .toURI()
                .toPath()
                .name
                .replace(sourceFile.nameWithoutExtension, destinationName, true)
            else
              it.url
                .toURI()
                .toPath()
                .name,
          )
        }

      var deletedUpgradedFile = false
      when {
        bookToUpgrade?.path != null && destFile == bookToUpgrade.path -> {
          logger.info { "Deleting existing file: ${bookToUpgrade.path}" }
          try {
            bookToUpgrade.path.deleteExisting()
            historicalEventRepository.insert(HistoricalEvent.BookFileDeleted(bookToUpgrade, "File was deleted to import an upgrade"))
            deletedUpgradedFile = true
          } catch (e: NoSuchFileException) {
            logger.warn { "Could not delete upgraded book: ${bookToUpgrade.path}" }
          }
        }

        destFile.exists() -> throw FileAlreadyExistsException("Destination file already exists: $destFile").withCode("ERR_1021")
      }
      // delete existing sidecars
      if (bookToUpgrade?.path != null) {
        fileSystemScanner.scanBookSidecars(bookToUpgrade.path).forEach { sidecar ->
          val sidecarPath = sidecar.url.toURI().toPath()
          logger.info { "Deleting existing file: $sidecarPath" }
          sidecarPath.deleteIfExists()
        }
      }

      when (copyMode) {
        CopyMode.MOVE -> {
          logger.info { "Moving file $sourceFile to $destFile" }
          sourceFile.moveTo(destFile)
          sidecars.forEach {
            it.key.url.toURI().toPath().let { sourcePath ->
              logger.info { "Moving file $sourcePath to ${it.value}" }
              sourcePath.moveTo(it.value, true)
            }
          }
        }

        CopyMode.COPY -> {
          logger.info { "Copying file $sourceFile to $destFile" }
          sourceFile.copyTo(destFile)
          sidecars.forEach {
            it.key.url.toURI().toPath().let { sourcePath ->
              logger.info { "Copying file $sourcePath to ${it.value}" }
              sourcePath.copyTo(it.value, true)
            }
          }
        }

        CopyMode.HARDLINK ->
          try {
            logger.info { "Hardlink file $sourceFile to $destFile" }
            Files.createLink(destFile, sourceFile)
            sidecars.forEach {
              it.key.url.toURI().toPath().let { sourcePath ->
                logger.info { "Hardlink file $sourcePath to ${it.value}" }
                it.value.deleteIfExists()
                Files.createLink(it.value, sourcePath)
              }
            }
          } catch (e: Exception) {
            logger.warn(e) { "Filesystem does not support hardlinks, copying instead" }
            sourceFile.copyTo(destFile)
            sidecars.forEach {
              it.key.url
                .toURI()
                .toPath()
                .copyTo(it.value, true)
            }
          }
      }

      val importedBook =
        fileSystemScanner
          .scanFile(destFile)
          ?.copy(libraryId = series.libraryId, oneshot = series.oneshot)
          ?: throw IllegalStateException("Newly imported book could not be scanned: $destFile").withCode("ERR_1022")

      seriesLifecycle.addBooks(series, listOf(importedBook))

      if (bookToUpgrade != null) {
        // copy media and mark it as outdated
        mediaRepository.findById(bookToUpgrade.id).let {
          mediaRepository.update(
            it.copy(
              bookId = importedBook.id,
              status = Media.Status.OUTDATED,
            ),
          )
        }

        // copy metadata
        metadataRepository.findById(bookToUpgrade.id).let {
          metadataRepository.update(it.copy(bookId = importedBook.id))
        }

        // copy user uploaded thumbnails
        thumbnailBookRepository.findAllByBookIdAndType(bookToUpgrade.id, setOf(ThumbnailBook.Type.USER_UPLOADED)).forEach { deleted ->
          thumbnailBookRepository.update(deleted.copy(bookId = importedBook.id))
        }

        // copy read progress
        readProgressRepository
          .findAllByBookId(bookToUpgrade.id)
          .map { it.copy(bookId = importedBook.id) }
          .forEach { readProgressRepository.save(it) }

        // replace upgraded book by imported book in read lists
        readListRepository
          .findAllContainingBookId(bookToUpgrade.id, filterOnLibraryIds = null)
          .forEach { rl ->
            readListRepository.update(
              rl.copy(
                bookIds =
                  rl.bookIds.values
                    .map { if (it == bookToUpgrade.id) importedBook.id else it }
                    .toIndexedMap(),
              ),
            )
          }

        // delete upgraded book file on disk if it has not been replaced earlier
        if (!deletedUpgradedFile && bookToUpgrade.path.deleteIfExists()) {
          logger.info { "Deleted existing file: ${bookToUpgrade.path}" }
          historicalEventRepository.insert(HistoricalEvent.BookFileDeleted(bookToUpgrade, "File was deleted to import an upgrade"))
        }

        // delete upgraded book
        bookLifecycle.deleteOne(bookToUpgrade)

        // update series if one-shot, so it's not marked as not found during the next scan
        if (series.oneshot) {
          seriesRepository.update(series.copy(url = importedBook.url, fileLastModified = importedBook.fileLastModified))
        }
      }

      seriesLifecycle.sortBooks(series)

      sidecars.forEach { (sourceSidecar, destPath) ->
        when (sourceSidecar.type) {
          Sidecar.Type.ARTWORK -> taskEmitter.refreshBookLocalArtwork(importedBook)
          Sidecar.Type.METADATA -> taskEmitter.refreshBookMetadata(importedBook)
        }
        val destSidecar =
          sourceSidecar.copy(
            url = destPath.toUri().toURL(),
            parentUrl = destPath.parent.toUri().toURL(),
            lastModifiedTime = destPath.readAttributes<BasicFileAttributes>().getUpdatedTime(),
          )
        sidecarRepository.save(importedBook.libraryId, destSidecar)
      }

      historicalEventRepository.insert(HistoricalEvent.BookImported(importedBook, series, sourceFile, upgradeBookId != null))
      eventPublisher.publishEvent(DomainEvent.BookImported(importedBook, sourceFile.toUri().toURL(), success = true))

      return importedBook
    } catch (e: Exception) {
      val msg = if (e is CodedException) e.code else e.message
      eventPublisher.publishEvent(DomainEvent.BookImported(null, sourceFile.toUri().toURL(), success = false, msg))
      throw e
    }
  }
}
