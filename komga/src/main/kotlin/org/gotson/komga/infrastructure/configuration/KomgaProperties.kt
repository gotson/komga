package org.gotson.komga.infrastructure.configuration

import jakarta.annotation.PostConstruct
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.sqlite.SQLiteConfig.JournalMode
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Component
@ConfigurationProperties(prefix = "komga")
@Validated
class KomgaProperties {
  @PostConstruct
  private fun makeDirs() {
    try {
      Path(database.file).parent.createDirectories()
    } catch (_: Exception) {
    }
  }

  var librariesScanCron: String = ""

  var librariesScanStartup: Boolean = false

  var librariesScanDirectoryExclusions: List<String> = emptyList()

  var deleteEmptyReadLists: Boolean = true

  var deleteEmptyCollections: Boolean = true

  @Positive
  var pageHashing: Int = 3

  var rememberMe = RememberMe()

  @DurationUnit(ChronoUnit.SECONDS)
  var sessionTimeout: Duration = Duration.ofMinutes(30)

  var oauth2AccountCreation: Boolean = false

  var oidcEmailVerification: Boolean = true

  var database = Database()

  var cors = Cors()

  var lucene = Lucene()

  var configDir: String? = null

  @Positive
  var taskConsumers: Int = 1

  @Positive
  var taskConsumersMax: Int = 1

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

    @get:Positive
    var poolSize: Int? = null

    @get:Positive
    var maxPoolSize: Int = 1

    var journalMode: JournalMode? = null

    @DurationUnit(ChronoUnit.SECONDS)
    var busyTimeout: Duration? = null

    var pragmas: Map<String, String> = emptyMap()
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
