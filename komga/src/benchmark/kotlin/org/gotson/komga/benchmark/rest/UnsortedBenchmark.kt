package org.gotson.komga.benchmark.rest

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.SeriesSearch
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Param
import org.springframework.data.domain.Pageable

open class UnsortedBenchmark : AbstractRestBenchmark() {
  @Param("2000")
  private var pageSize: Int = DEFAULT_PAGE_SIZE

  @Benchmark
  fun getAllSeries() {
    seriesController.getSeries(principal, page = Pageable.ofSize(pageSize), search = SeriesSearch())
  }

  @Benchmark
  fun getAllBooks() {
    bookController.getBooks(principal, page = Pageable.ofSize(pageSize), search = BookSearch())
  }
}
