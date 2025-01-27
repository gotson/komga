package org.gotson.komga.benchmark

import org.junit.jupiter.api.Test
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

@SpringBootTest
@ActiveProfiles("benchmark")
@State(Scope.Benchmark)
abstract class AbstractBenchmark {
  @Autowired
  private lateinit var benchmarkProperties: BenchmarkProperties

  @Test
  fun executeJmhRunner() {
    if (this.javaClass.simpleName.contains("jmhType")) return
    Files.createDirectories(Paths.get(benchmarkProperties.resultFolder))
    val extension =
      when (benchmarkProperties.resultFormat) {
        ResultFormatType.TEXT -> "txt"
        ResultFormatType.CSV -> "csv"
        ResultFormatType.SCSV -> "scsv"
        ResultFormatType.JSON -> "json"
        ResultFormatType.LATEX -> "tex"
      }
    val resultFile = Paths.get(benchmarkProperties.resultFolder, "${this.javaClass.name}.$extension").absolutePathString()
    val opt: Options =
      OptionsBuilder()
        .include("\\." + this.javaClass.simpleName + "\\.") // set the class name regex for benchmarks to search for to the current class
        .apply { if (benchmarkProperties.warmupIterations > 0) warmupIterations(benchmarkProperties.warmupIterations) }
        .measurementIterations(benchmarkProperties.measurementIterations)
        .forks(0) // do not use forking or the benchmark methods will not see references stored within its class
        .threads(1) // do not use multiple threads
        .mode(benchmarkProperties.mode)
        .shouldDoGC(true)
        .shouldFailOnError(true)
        .resultFormat(benchmarkProperties.resultFormat)
        .result(resultFile)
        .shouldFailOnError(true)
        .jvmArgs("-server")
        .build()
    Runner(opt).run()
  }
}
