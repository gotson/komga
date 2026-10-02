package org.gotson.komga.benchmark.rest

import org.gotson.komga.domain.model.BookSearch
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Param
import org.springframework.data.domain.PageRequest

open class BrowseBooksBenchmark : AbstractRestBenchmark() {
  @Param("50", "500")
  private var pageSize: Int = DEFAULT_PAGE_SIZE

  @Benchmark
  fun browseSeriesBooks() {
    bookController.getBooks(
      principal,
      page = PageRequest.of(0, pageSize),
      search = BookSearch(),
    )
  }
}
