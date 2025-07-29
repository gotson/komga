package org.gotson.komga.benchmark.rest

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.SearchCondition
import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.domain.model.SeriesSearch
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Setup
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.concurrent.TimeUnit

@OutputTimeUnit(TimeUnit.MILLISECONDS)
class BrowseBenchmark : AbstractRestBenchmark() {
  companion object {
    private lateinit var biggestSeriesId: String
  }

  @Param("20", "50", "100", "200", "500")
  private var pageSize: Int = DEFAULT_PAGE_SIZE

  @Setup(Level.Trial)
  override fun prepareData() {
    super.prepareData()

    // find series with most books
    biggestSeriesId =
      seriesController
        .getSeries(principal, page = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("booksCount"))), search = SeriesSearch())
        .content
        .first()
        .id
  }

  @Benchmark
  fun browseSeries() {
    seriesController.getSeries(principal, page = PageRequest.of(0, pageSize, Sort.by(Sort.Order.asc("metadata.titleSort"))), search = SeriesSearch())
  }

  @Benchmark
  fun browseSeriesBooks() {
    bookController.getBooks(principal, page = PageRequest.of(0, pageSize, Sort.by(Sort.Order.asc("metadata.numberSort"))), search = BookSearch(SearchCondition.SeriesId(SearchOperator.Is(biggestSeriesId))))
  }
}
