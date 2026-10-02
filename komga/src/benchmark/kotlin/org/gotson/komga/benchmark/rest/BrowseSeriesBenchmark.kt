package org.gotson.komga.benchmark.rest

import org.gotson.komga.domain.model.SeriesSearch
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Param
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

open class BrowseSeriesBenchmark : AbstractRestBenchmark() {
  @Param("50", "500")
  private var pageSize: Int = DEFAULT_PAGE_SIZE

  @Benchmark
  fun browseSeries() {
    seriesController.getSeries(principal, page = PageRequest.of(0, pageSize, Sort.by(Sort.Order.asc("metadata.titleSort"))), search = SeriesSearch())
  }
}
