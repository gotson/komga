package org.gotson.komga.application.tasks

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.gotson.komga.domain.model.BookAction
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.service.BookConverter
import org.gotson.komga.domain.service.BookImporter
import org.gotson.komga.domain.service.BookLifecycle
import org.gotson.komga.domain.service.BookMetadataLifecycle
import org.gotson.komga.domain.service.BookPageEditor
import org.gotson.komga.domain.service.LibraryContentLifecycle
import org.gotson.komga.domain.service.LocalArtworkLifecycle
import org.gotson.komga.domain.service.PageHashLifecycle
import org.gotson.komga.domain.service.SeriesLifecycle
import org.gotson.komga.domain.service.SeriesMetadataLifecycle
import org.gotson.komga.domain.service.ThumbnailLifecycle
import org.gotson.komga.infrastructure.search.SearchIndexLifecycle
import org.gotson.komga.interfaces.scheduler.METER_TASKS_EXECUTION
import org.gotson.komga.interfaces.scheduler.METER_TASKS_FAILURE
import org.springframework.stereotype.Service
import java.nio.file.Paths
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger {}

@Service
class TaskHandler(
  private val taskEmitter: TaskEmitter,
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val seriesRepository: SeriesRepository,
  private val libraryContentLifecycle: LibraryContentLifecycle,
  private val bookLifecycle: BookLifecycle,
  private val bookMetadataLifecycle: BookMetadataLifecycle,
  private val seriesLifecycle: SeriesLifecycle,
  private val seriesMetadataLifecycle: SeriesMetadataLifecycle,
  private val localArtworkLifecycle: LocalArtworkLifecycle,
  private val bookImporter: BookImporter,
  private val bookConverter: BookConverter,
  private val bookPageEditor: BookPageEditor,
  private val searchIndexLifecycle: SearchIndexLifecycle,
  private val pageHashLifecycle: PageHashLifecycle,
  private val thumbnailLifecycle: ThumbnailLifecycle,
  private val meterRegistry: MeterRegistry,
) {
  fun handleTask(task: Task) {
    logger.info { "Executing task: $task" }
    try {
      measureTime {
        when (task) {
          is Task.ScanLibrary ->
            libraryRepository.findByIdOrNull(task.libraryId)?.let { library ->
              libraryContentLifecycle.scanRootFolder(library, task.scanDeep)
              taskEmitter.analyzeUnknownAndOutdatedBooks(library)
              taskEmitter.repairExtensions(library, LOW_PRIORITY)
              taskEmitter.findBooksToConvert(library, LOWEST_PRIORITY)
              taskEmitter.findBooksWithMissingPageHash(library, LOWEST_PRIORITY)
              taskEmitter.findDuplicatePagesToDelete(library, LOWEST_PRIORITY)
              taskEmitter.hashBooksWithoutHash(library)
              taskEmitter.hashBooksWithoutHashKoreader(library)
            } ?: logger.warn { "Cannot execute task $task: Library does not exist" }

          is Task.FindBooksToConvert ->
            libraryRepository.findByIdOrNull(task.libraryId)?.let { library ->
              taskEmitter.convertBookToCbz(bookConverter.getConvertibleBooks(library), task.priority + 1)
            } ?: logger.warn { "Cannot execute task $task: Library does not exist" }

          is Task.FindBooksWithMissingPageHash ->
            libraryRepository.findByIdOrNull(task.libraryId)?.let { library ->
              taskEmitter.hashBookPages(pageHashLifecycle.getBookIdsWithMissingPageHash(library), task.priority + 1)
            } ?: logger.warn { "Cannot execute task $task: Library does not exist" }

          is Task.FindDuplicatePagesToDelete ->
            libraryRepository.findByIdOrNull(task.libraryId)?.let { library ->
              taskEmitter.removeDuplicatePages(pageHashLifecycle.getBookPagesToDeleteAutomatically(library), task.priority + 1)
            } ?: logger.warn { "Cannot execute task $task: Library does not exist" }

          is Task.EmptyTrash ->
            libraryRepository.findByIdOrNull(task.libraryId)?.let { library ->
              libraryContentLifecycle.emptyTrash(library)
            } ?: logger.warn { "Cannot execute task $task: Library does not exist" }

          is Task.AnalyzeBook ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              val actions = bookLifecycle.analyzeAndPersist(book)
              if (actions.contains(BookAction.GENERATE_THUMBNAIL)) taskEmitter.generateBookThumbnail(book.id, priority = task.priority + 1)
              if (actions.contains(BookAction.REFRESH_METADATA)) taskEmitter.refreshBookMetadata(book, priority = task.priority + 1)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.GenerateBookThumbnail ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              bookLifecycle.generateThumbnailAndPersist(book)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.RefreshBookMetadata ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              bookMetadataLifecycle.refreshMetadata(book, task.capabilities)
              taskEmitter.refreshSeriesMetadata(book.seriesId, priority = task.priority - 1)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.RefreshSeriesMetadata ->
            seriesRepository.findByIdOrNull(task.seriesId)?.let { series ->
              seriesMetadataLifecycle.refreshMetadata(series)
              taskEmitter.aggregateSeriesMetadata(series.id, priority = task.priority)
            } ?: logger.warn { "Cannot execute task $task: Series does not exist" }

          is Task.AggregateSeriesMetadata ->
            seriesRepository.findByIdOrNull(task.seriesId)?.let { series ->
              seriesMetadataLifecycle.aggregateMetadata(series)
            } ?: logger.warn { "Cannot execute task $task: Series does not exist" }

          is Task.RefreshBookLocalArtwork ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              localArtworkLifecycle.refreshLocalArtwork(book)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.RefreshSeriesLocalArtwork ->
            seriesRepository.findByIdOrNull(task.seriesId)?.let { series ->
              localArtworkLifecycle.refreshLocalArtwork(series)
            } ?: logger.warn { "Cannot execute task $task: Series does not exist" }

          is Task.ImportBook ->
            seriesRepository.findByIdOrNull(task.seriesId)?.let { series ->
              val importedBook = bookImporter.importBook(Paths.get(task.sourceFile), series, task.copyMode, task.destinationName, task.upgradeBookId)
              taskEmitter.analyzeBook(importedBook, priority = task.priority + 1)
            } ?: logger.warn { "Cannot execute task $task: Series does not exist" }

          is Task.ConvertBook ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              bookConverter.convertToCbz(book)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.RepairExtension ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              bookConverter.repairExtension(book)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.RemoveHashedPages ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              if (bookPageEditor.removeHashedPages(book, task.pages) == BookAction.GENERATE_THUMBNAIL) {
                taskEmitter.generateBookThumbnail(book.id, priority = task.priority + 1)
              }
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.HashBook ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              bookLifecycle.hashAndPersist(book)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.HashBookKoreader ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              bookLifecycle.hashKoreaderAndPersist(book)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.HashBookPages ->
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              bookLifecycle.hashPagesAndPersist(book)
            } ?: logger.warn { "Cannot execute task $task: Book does not exist" }

          is Task.RebuildIndex -> searchIndexLifecycle.rebuildIndex(task.entities)

          is Task.UpgradeIndex -> searchIndexLifecycle.upgradeIndex()

          is Task.DeleteBook -> {
            bookRepository.findByIdOrNull(task.bookId)?.let { book ->
              if (book.oneshot)
                seriesLifecycle.deleteSeriesFiles(seriesRepository.findByIdOrNull(book.seriesId)!!)
              else
                bookLifecycle.deleteBookFiles(book)
            }
          }

          is Task.DeleteSeries -> {
            seriesRepository.findByIdOrNull(task.seriesId)?.let { series ->
              seriesLifecycle.deleteSeriesFiles(series)
            }
          }

          is Task.FixThumbnailsWithoutMetadata -> {
            if (thumbnailLifecycle.fixThumbnailsMetadata())
              taskEmitter.fixThumbnailsWithoutMetadata(LOWEST_PRIORITY)
          }

          is Task.FindBookThumbnailsToRegenerate -> {
            taskEmitter.generateBookThumbnail(bookLifecycle.findBookThumbnailsToRegenerate(task.forBiggerResultOnly), task.priority)
          }
        }
      }.also {
        logger.info { "Task $task executed in $it" }
        meterRegistry.timer(METER_TASKS_EXECUTION, "type", task.javaClass.simpleName).record(it.toJavaDuration())
      }
    } catch (e: Exception) {
      logger.error(e) { "Task $task execution failed" }
      meterRegistry.counter(METER_TASKS_FAILURE, "type", task.javaClass.simpleName).increment()
    }
  }
}
