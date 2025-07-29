package org.gotson.komga.benchmark.rest

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.SeriesSearch
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Setup
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.concurrent.TimeUnit

@OutputTimeUnit(TimeUnit.MILLISECONDS)
class UnsortedBenchmark : AbstractRestBenchmark() {
  companion object {
    private lateinit var biggestSeriesId: String
  }

  private var pageSize: Int = 2000

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
  fun getAllSeries() {
    seriesController.getSeries(principal, page = Pageable.ofSize(pageSize), search = SeriesSearch())
  }

  @Benchmark
  fun getAllBooks() {
    bookController.getBooks(principal, page = Pageable.ofSize(pageSize), search = BookSearch())
  }
}
