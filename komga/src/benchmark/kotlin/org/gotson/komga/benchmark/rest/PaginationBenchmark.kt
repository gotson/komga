package org.gotson.komga.benchmark.rest

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Setup
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.concurrent.TimeUnit

@OutputTimeUnit(TimeUnit.MILLISECONDS)
class PaginationBenchmark : AbstractRestBenchmark() {

  companion object {
    private lateinit var biggestSeriesId: String
  }

  @Param("20", "100", "500", "1000", "5000")
  private var pageSize: Int = DEFAULT_PAGE_SIZE

  @Setup(Level.Trial)
  override fun prepareData() {
    super.prepareData()

    // find series with most books
    biggestSeriesId = seriesController.getAllSeries(principal, page = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("booksCount"))))
      .content.first()
      .id
  }

  @Benchmark
  fun getAllSeries() {
    seriesController.getAllSeries(principal, page = Pageable.ofSize(pageSize))
  }

  @Benchmark
  fun getAllBooks() {
    bookController.getAllBooks(principal, page = Pageable.ofSize(pageSize))
  }

  @Benchmark
  fun getSeriesBooks() {
    seriesController.getAllBooksBySeries(principal, biggestSeriesId, page = Pageable.ofSize(pageSize))
  }
}
