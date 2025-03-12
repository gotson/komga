package org.gotson.komga.benchmark.rest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.interfaces.api.rest.dto.ReadProgressUpdateDto
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Setup
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@OutputTimeUnit(TimeUnit.MILLISECONDS)
class DashboardBenchmark : AbstractRestBenchmark() {
  companion object {
    lateinit var bookLatestReleaseDate: LocalDate
  }

  @Setup(Level.Trial)
  override fun prepareData() {
    super.prepareData()

    // mark some books in progress
    bookController.getAllBooksDeprecated(principal, readStatus = listOf(ReadStatus.IN_PROGRESS), page = Pageable.ofSize(DEFAULT_PAGE_SIZE)).let { page ->
      if (page.totalElements < DEFAULT_PAGE_SIZE) {
        bookController.getAllBooksDeprecated(principal, readStatus = listOf(ReadStatus.UNREAD), page = Pageable.ofSize(DEFAULT_PAGE_SIZE)).content.forEach { book ->
          bookController.markBookReadProgress(book.id, ReadProgressUpdateDto(2, false), principal)
        }
      }
    }

    // mark some books read for on deck
    bookController.getBooksOnDeck(principal, page = Pageable.ofSize(DEFAULT_PAGE_SIZE)).let { page ->
      if (page.totalElements < DEFAULT_PAGE_SIZE) {
        seriesController.getSeriesDeprecated(principal, readStatus = listOf(ReadStatus.UNREAD), oneshot = false, page = Pageable.ofSize(DEFAULT_PAGE_SIZE)).content.forEach { series ->
          val book = seriesController.getBooksBySeriesId(principal, series.id, page = Pageable.ofSize(1)).content.first()
          bookController.markBookReadProgress(book.id, ReadProgressUpdateDto(null, true), principal)
        }
      }
    }

    // retrieve most recent book release date
    bookLatestReleaseDate = bookController
      .getAllBooksDeprecated(principal, page = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("metadata.releaseDate"))))
      .content
      .firstOrNull()
      ?.metadata
      ?.releaseDate ?: LocalDate.now()
  }

  @Benchmark
  fun loadDashboard() {
    runBlocking {
      withContext(Dispatchers.Default) {
        launch { getBooksInProgress() }
        launch { getBooksOnDeck() }
        launch { getBooksLatest() }
        launch { getBooksRecentlyReleased() }
        launch { getSeriesNew() }
        launch { getSeriesUpdated() }
      }
    }
  }

  val pageableBooksInProgress = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Order.desc("readProgress.readDate")))

  @Benchmark
  fun getBooksInProgress() {
    bookController.getAllBooksDeprecated(principal, readStatus = listOf(ReadStatus.IN_PROGRESS), page = pageableBooksInProgress)
  }

  val pageableBooksOnDeck = Pageable.ofSize(DEFAULT_PAGE_SIZE)

  @Benchmark
  fun getBooksOnDeck() {
    bookController.getBooksOnDeck(principal, page = pageableBooksOnDeck)
  }

  val pageableBooksLatest = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Order.desc("createdDate")))

  @Benchmark
  fun getBooksLatest() {
    bookController.getAllBooksDeprecated(principal, page = pageableBooksLatest)
  }

  val pageableBooksRecentlyReleased = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Order.desc("metadata.releaseDate")))

  @Benchmark
  fun getBooksRecentlyReleased() {
    bookController.getAllBooksDeprecated(principal, releasedAfter = bookLatestReleaseDate.minusMonths(1), page = pageableBooksRecentlyReleased)
  }

  val pageableSeriesNew = Pageable.ofSize(DEFAULT_PAGE_SIZE)

  @Benchmark
  fun getSeriesNew() {
    seriesController.getSeriesNew(principal, page = pageableSeriesNew)
  }

  val pageableSeriesUpdated = Pageable.ofSize(DEFAULT_PAGE_SIZE)

  @Benchmark
  fun getSeriesUpdated() {
    seriesController.getSeriesUpdated(principal, page = pageableSeriesUpdated)
  }

  val pageableBooksToCheck = Pageable.ofSize(1)

  @Benchmark
  fun getBooksToCheck() {
    bookController.getAllBooksDeprecated(principal, mediaStatus = listOf(Media.Status.ERROR, Media.Status.UNSUPPORTED), page = pageableBooksToCheck)
  }
}
