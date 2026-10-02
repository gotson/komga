package org.gotson.komga.benchmark

import org.gotson.komga.Application
import org.junit.jupiter.api.Test
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(1)
abstract class AbstractBenchmark {
  protected lateinit var context: ConfigurableApplicationContext

  @Setup(Level.Trial)
  fun setupSpringContext() {
    context =
      SpringApplicationBuilder(Application::class.java)
        .profiles("benchmark", "test")
        .run()

    // Populates @Autowired fields on the benchmark class within the forked JVM
    context.autowireCapableBeanFactory.autowireBean(this)
  }

  @TearDown(Level.Trial)
  fun tearDownSpringContext() {
    if (::context.isInitialized) context.close()
  }

  @Test
  fun executeJmhRunner() {
    if (this.javaClass.simpleName.contains("jmhType")) return

    val resultFolder =
      System.getProperty("jmh.resultFolder")
        ?: Paths.get("build", "reports", "benchmarks").absolutePathString()

    Files.createDirectories(Paths.get(resultFolder))
    val resultFile = Paths.get(resultFolder, "${this.javaClass.name}.txt").absolutePathString()

    val opt: Options =
      OptionsBuilder()
        .include("\\." + this.javaClass.simpleName + "\\.") // set the class name regex for benchmarks to search for to the current class
        .threads(1) // do not use multiple threads
        .shouldDoGC(true)
        .shouldFailOnError(true)
        .resultFormat(ResultFormatType.TEXT)
        .result(resultFile)
        .jvmArgs("-server")
        .build()

    Runner(opt).run()
  }
}
