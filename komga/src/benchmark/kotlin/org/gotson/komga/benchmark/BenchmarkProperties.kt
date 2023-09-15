package org.gotson.komga.benchmark

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.results.format.ResultFormatType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "benchmark")
@Validated
class BenchmarkProperties {
  @PositiveOrZero
  var warmupIterations: Int = 1

  @Positive
  var measurementIterations: Int = 1

  var resultFolder: String = ""

  var resultFormat: ResultFormatType = ResultFormatType.TEXT

  var mode: Mode = Mode.AverageTime
}
