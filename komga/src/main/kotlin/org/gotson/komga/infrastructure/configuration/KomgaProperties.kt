package org.gotson.komga.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.time.Duration
import java.time.temporal.ChronoUnit
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

@Component
@ConfigurationProperties(prefix = "komga")
@Validated
class KomgaProperties {
  var librariesScanCron: String = ""

  var librariesScanStartup: Boolean = false

  var librariesScanDirectoryExclusions: List<String> = emptyList()

  var deleteEmptyReadLists: Boolean = true

  var deleteEmptyCollections: Boolean = true

  var fileHashing: Boolean = true

  var rememberMe = RememberMe()

  @DurationUnit(ChronoUnit.SECONDS)
  var sessionTimeout: Duration = Duration.ofMinutes(30)

  var nativeWebp: Boolean = true

  var database = Database()

  var cors = Cors()

  var lucene = Lucene()

  class RememberMe {
    @get:NotBlank
    var key: String? = null

    @DurationUnit(ChronoUnit.SECONDS)
    var validity: Duration = Duration.ofDays(14)
  }

  class Cors {
    var allowedOrigins: List<String> = emptyList()
  }

  class Database {
    @get:NotBlank
    var file: String = ""

    @get:Positive
    var batchChunkSize: Int = 1000
  }

  class Lucene {
    @get:NotBlank
    var dataDirectory: String = ""

    var indexAnalyzer = IndexAnalyzer()

    class IndexAnalyzer {
      @get:Positive
      var minGram: Int = 3

      @get:Positive
      var maxGram: Int = 10

      var preserveOriginal: Boolean = true
    }
  }
}
