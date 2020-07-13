package org.gotson.komga.domain.service

import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.apache.commons.lang3.StringUtils
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
import org.gotson.komga.domain.persistence.SeriesCollectionRepository
import org.gotson.komga.domain.persistence.SeriesMetadataRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}
private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

@Service
class SeriesLifecycle(
  private val bookRepository: BookRepository,
  private val bookLifecycle: BookLifecycle,
  private val mediaRepository: MediaRepository,
  private val bookMetadataRepository: BookMetadataRepository,
  private val seriesRepository: SeriesRepository,
  private val seriesMetadataRepository: SeriesMetadataRepository,
  private val collectionRepository: SeriesCollectionRepository
) {

  fun sortBooks(series: Series) {
    val books = bookRepository.findBySeriesId(series.id)
    val metadatas = bookMetadataRepository.findByIds(books.map { it.id })

    val sorted = books
      .sortedWith(compareBy(natSortComparator) { it.name })
      .map { book -> book to metadatas.first { it.bookId == book.id } }

    bookRepository.updateMany(
      sorted.mapIndexed { index, (book, _) -> book.copy(number = index + 1) }
    )

    sorted.mapIndexedNotNull { index, (_, metadata) ->
      if (metadata.numberLock && metadata.numberSortLock) null
      else metadata.copy(
        number = if (!metadata.numberLock) (index + 1).toString() else metadata.number,
        numberSort = if (!metadata.numberSortLock) (index + 1).toFloat() else metadata.numberSort
      )
    }.let { bookMetadataRepository.updateMany(it) }
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

    seriesRepository.delete(seriesId)
  }

  fun deleteMany(seriesIds: Collection<String>) {
    logger.info { "Delete series ids: $seriesIds" }

    val bookIds = bookRepository.findAllIdBySeriesIds(seriesIds)
    bookLifecycle.deleteMany(bookIds)

    collectionRepository.removeSeriesFromAll(seriesIds)

    seriesRepository.deleteAll(seriesIds)
  }
}
