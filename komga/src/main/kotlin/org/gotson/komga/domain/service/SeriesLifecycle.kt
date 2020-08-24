package org.gotson.komga.domain.service

import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.apache.commons.lang3.StringUtils
import org.gotson.komga.application.tasks.TaskReceiver
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.ThumbnailSeries
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.gotson.komga.domain.persistence.ThumbnailSeriesRepository
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Comparator

private val logger = KotlinLogging.logger {}
private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

@Service
class SeriesLifecycle(
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesRepository: SeriesRepository,
  private val thumbnailsSeriesRepository: ThumbnailSeriesRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val collectionRepository: SeriesCollectionRepository,
  private val taskReceiver: TaskReceiver
) {

  fun sortBooks(series: Series) {
    logger.debug { "Sorting books for $series" }

    val books = bookRepository.findBySeriesId(series.id)
    val metadatas = bookMetadataRepository.findByIds(books.map { it.id })
    logger.debug { "Existing books: $books" }
    logger.debug { "Existing metadata: $metadatas" }

    val sorted = books
      .sortedWith(compareBy(natSortComparator) { it.name })
      .map { book -> book to metadatas.first { it.bookId == book.id } }
    logger.debug { "Sorted books: $sorted" }

    bookRepository.updateMany(
      sorted.mapIndexed { index, (book, _) -> book.copy(number = index + 1) }
    )

    val oldToNew = sorted.mapIndexedNotNull { index, (_, metadata) ->
      if (metadata.numberLock && metadata.numberSortLock) null
      else metadata to metadata.copy(
        number = if (!metadata.numberLock) (index + 1).toString() else metadata.number,
        numberSort = if (!metadata.numberSortLock) (index + 1).toFloat() else metadata.numberSort
      )
    }
    bookMetadataRepository.updateMany(oldToNew.map { it.second })

    oldToNew.forEach { (old, new) ->
      if (old.number != new.number || old.numberSort != new.numberSort) {
        logger.debug { "Metadata numbering has changed, refreshing metadata for book ${new.bookId} " }
        taskReceiver.refreshBookMetadata(new.bookId)
      }
    }
  }

  fun addBooks(series: Series, booksToAdd: Collection<Book>) {
    booksToAdd.forEach {
      check(it.libraryId == series.libraryId) { "Cannot add book to series if they don't share the same libraryId" }
    }

    bookRepository.insertMany(
      booksToAdd.map { it.copy(seriesId = series.id) }
    )

    // create associated media
    mediaRepository.insertMany(booksToAdd.map { Media(bookId = it.id) })

    // create associated metadata
    booksToAdd.map {
      BookMetadata(
        title = it.name,
        number = it.number.toString(),
        numberSort = it.number.toFloat(),
        bookId = it.id
      )
    }.let { bookMetadataRepository.insertMany(it) }
  }

  fun createSeries(series: Series): Series {
    seriesRepository.insert(series)

    seriesMetadataRepository.insert(
      SeriesMetadata(
        title = series.name,
        titleSort = StringUtils.stripAccents(series.name),
        seriesId = series.id
      )
    )

    return seriesRepository.findByIdOrNull(series.id)!!
  }

  fun deleteOne(seriesId: String) {
    logger.info { "Delete series id: $seriesId" }

    val bookIds = bookRepository.findAllIdBySeriesId(seriesId)
    bookLifecycle.deleteMany(bookIds)

    collectionRepository.removeSeriesFromAll(seriesId)
    thumbnailsSeriesRepository.deleteBySeriesId(seriesId)
    seriesMetadataRepository.delete(seriesId)

    seriesRepository.delete(seriesId)
  }

  fun deleteMany(seriesIds: Collection<String>) {
    logger.info { "Delete series ids: $seriesIds" }

    val bookIds = bookRepository.findAllIdBySeriesIds(seriesIds)
    bookLifecycle.deleteMany(bookIds)

    collectionRepository.removeSeriesFromAll(seriesIds)
    thumbnailsSeriesRepository.deleteBySeriesIds(seriesIds)
    seriesMetadataRepository.delete(seriesIds)

    seriesRepository.deleteAll(seriesIds)
  }

  fun getThumbnail(seriesId: String): ThumbnailSeries? {
    val selected = thumbnailsSeriesRepository.findSelectedBySeriesId(seriesId)

    if (selected == null || !selected.exists()) {
      thumbnailsHouseKeeping(seriesId)
      return thumbnailsSeriesRepository.findSelectedBySeriesId(seriesId)
    }

    return selected
  }

  fun getThumbnailBytes(seriesId: String): ByteArray? {
    getThumbnail(seriesId)?.let {
      return File(it.url.toURI()).readBytes()
    }

    bookRepository.findFirstIdInSeries(seriesId)?.let { bookId ->
      return bookLifecycle.getThumbnailBytes(bookId)
    }
    return null
  }

  fun addThumbnailForSeries(thumbnail: ThumbnailSeries) {
    // delete existing thumbnail with the same url
    thumbnailsSeriesRepository.findBySeriesId(thumbnail.seriesId)
      .filter { it.url == thumbnail.url }
      .forEach {
        thumbnailsSeriesRepository.delete(it.id)
      }
    thumbnailsSeriesRepository.insert(thumbnail)

    if (thumbnail.selected)
      thumbnailsSeriesRepository.markSelected(thumbnail)
  }

  private fun thumbnailsHouseKeeping(seriesId: String) {
    logger.info { "House keeping thumbnails for series: $seriesId" }
    val all = thumbnailsSeriesRepository.findBySeriesId(seriesId)
      .mapNotNull {
        if (!it.exists()) {
          logger.warn { "Thumbnail doesn't exist, removing entry" }
          thumbnailsSeriesRepository.delete(it.id)
          null
        } else it
      }

    val selected = all.filter { it.selected }
    when {
      selected.size > 1 -> {
        logger.info { "More than one thumbnail is selected, removing extra ones" }
        thumbnailsSeriesRepository.markSelected(selected[0])
      }
      selected.isEmpty() && all.isNotEmpty() -> {
        logger.info { "Series has bo selected thumbnail, choosing one automatically" }
        thumbnailsSeriesRepository.markSelected(all.first())
      }
    }
  }

  private fun ThumbnailSeries.exists(): Boolean = Files.exists(Paths.get(url.toURI()))
}
