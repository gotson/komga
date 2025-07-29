package org.gotson.komga.benchmark.rest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.ReadStatus
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.SeriesSearch
import org.gotson.komga.interfaces.api.rest.dto.ReadProgressUpdateDto
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Setup
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@OutputTimeUnit(TimeUnit.MILLISECONDS)
class DashboardBenchmark : AbstractRestBenchmark() {
  companion object {
    lateinit var bookLatestReleaseDate: ZonedDateTime
  }

  @Setup(Level.Trial)
  override fun prepareData() {
    super.prepareData()

    // mark some books in progress
    bookController.getBooks(principal, page = Pageable.ofSize(DEFAULT_PAGE_SIZE), search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS)))).let { page ->
      if (page.totalElements < DEFAULT_PAGE_SIZE) {
        bookController.getBooks(principal, page = Pageable.ofSize(DEFAULT_PAGE_SIZE), search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)))).content.forEach { book ->
          bookController.markBookReadProgress(book.id, ReadProgressUpdateDto(2, false), principal)
        }
      }
    }

    // mark some books read for on deck
    bookController.getBooksOnDeck(principal, page = Pageable.ofSize(DEFAULT_PAGE_SIZE)).let { page ->
      if (page.totalElements < DEFAULT_PAGE_SIZE) {
        seriesController
          .getSeries(
            principal,
            page = Pageable.ofSize(DEFAULT_PAGE_SIZE),
            search =
              SeriesSearch(
                SearchCondition.AllOfSeries(
                  SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.UNREAD)),
                  SearchCondition.OneShot(SearchOperator.IsFalse),
                ),
              ),
          ).content
          .forEach { series ->
            val book = bookController.getBooks(principal, page = Pageable.ofSize(1), search = BookSearch(SearchCondition.SeriesId(SearchOperator.Is(series.id)))).content.first()
            bookController.markBookReadProgress(book.id, ReadProgressUpdateDto(null, true), principal)
          }
      }
    }

    // retrieve most recent book release date
    bookLatestReleaseDate = bookController
      .getBooks(principal, page = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("metadata.releaseDate"))), search = BookSearch())
      .content
      .firstOrNull()
      ?.metadata
      ?.releaseDate
      ?.atStartOfDay(ZoneOffset.UTC) ?: ZonedDateTime.now()
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
    bookController.getBooks(principal, page = pageableBooksInProgress, search = BookSearch(SearchCondition.ReadStatus(SearchOperator.Is(ReadStatus.IN_PROGRESS))))
  }

  val pageableBooksOnDeck = Pageable.ofSize(DEFAULT_PAGE_SIZE)

  @Benchmark
  fun getBooksOnDeck() {
    bookController.getBooksOnDeck(principal, page = pageableBooksOnDeck)
  }

  val pageableBooksLatest = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Order.desc("createdDate")))

  @Benchmark
  fun getBooksLatest() {
    bookController.getBooks(principal, page = pageableBooksLatest, search = BookSearch())
  }

  val pageableBooksRecentlyReleased = PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Order.desc("metadata.releaseDate")))

  @Benchmark
  fun getBooksRecentlyReleased() { // releasedAfter = bookLatestReleaseDate.minusMonths(1)
    bookController.getBooks(principal, page = pageableBooksRecentlyReleased, search = BookSearch(SearchCondition.ReleaseDate(SearchOperator.After(bookLatestReleaseDate.minusMonths(1)))))
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
    bookController.getBooks(
      principal,
      page = pageableBooksToCheck,
      search =
        BookSearch(
          SearchCondition.AnyOfBook(
            SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.ERROR)),
            SearchCondition.MediaStatus(SearchOperator.Is(Media.Status.UNSUPPORTED)),
          ),
        ),
    )
  }
}
