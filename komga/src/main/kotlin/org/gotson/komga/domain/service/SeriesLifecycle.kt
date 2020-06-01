package org.gotson.komga.domain.service

import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.Book
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.Series
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.persistence.BookMetadataRepository
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.MediaRepository
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
  private val seriesMetadataRepository: SeriesMetadataRepository
) {

  fun sortBooks(series: Series) {
    val books = bookRepository.findBySeriesId(series.id)

    val sorted = books.sortedWith(compareBy(natSortComparator) { it.name })
    sorted.forEachIndexed { index, book ->
      val number = index + 1
      bookRepository.update(book.copy(number = number))

      bookMetadataRepository.findById(book.id).let { metadata ->
        val renumbered = metadata.copy(
          number = if (!metadata.numberLock) number.toString() else metadata.number,
          numberSort = if (!metadata.numberSortLock) number.toFloat() else metadata.numberSort
        )
        if (!metadata.numberLock || !metadata.numberSortLock)
          bookMetadataRepository.update(renumbered)
      }
    }
  }

  fun addBooks(series: Series, booksToAdd: Collection<Book>) {
    booksToAdd.forEach {
      check(it.libraryId == series.libraryId) { "Cannot add book to series if they don't share the same libraryId" }
    }

    booksToAdd.forEach { book ->
      val createdBook = bookRepository.insert(book.copy(seriesId = series.id))

      // create associated media
      mediaRepository.insert(Media(bookId = createdBook.id))

      // create associated metadata
      bookMetadataRepository.insert(BookMetadata(
        title = createdBook.name,
        number = createdBook.number.toString(),
        numberSort = createdBook.number.toFloat(),
        bookId = createdBook.id
      ))
    }
  }

  fun createSeries(series: Series): Series {
    val createdSeries = seriesRepository.insert(series)

    seriesMetadataRepository.insert(
      SeriesMetadata(
        title = createdSeries.name,
        seriesId = createdSeries.id
      )
    )

    return createdSeries
  }

  fun deleteSeries(seriesId: Long) {
    logger.info { "Delete series id: $seriesId" }

    bookRepository.findBySeriesId(seriesId).forEach {
      bookLifecycle.delete(it.id)
    }

    seriesRepository.delete(seriesId)
  }
}
