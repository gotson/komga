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

  @Deprecated("Moved to library options since 1.5.0")
  var librariesScanCron: String = ""

  @Deprecated("Moved to library options since 1.5.0")
  var librariesScanStartup: Boolean = false

  @Deprecated("Moved to library options since 1.5.0")
  var librariesScanDirectoryExclusions: List<String> = emptyList()

  @Deprecated("Moved to server settings since 1.5.0")
  var deleteEmptyReadLists: Boolean = true

  @Deprecated("Moved to server settings since 1.5.0")
  var deleteEmptyCollections: Boolean = true

  @Positive
  var pageHashing: Int = 3

  @Positive
  var epubDivinaLetterCountThreshold: Int = 15

  @Deprecated("Moved to server settings since 1.5.0")
  var rememberMe = RememberMe()

  @Deprecated("Removed since 1.5.0", ReplaceWith("server.servlet.session.timeout"))
  @DurationUnit(ChronoUnit.SECONDS)
  var sessionTimeout: Duration = Duration.ofMinutes(30)

  var oauth2AccountCreation: Boolean = false

  var oidcEmailVerification: Boolean = true

  var database = Database()

  var tasksDb = Database()

  var cors = Cors()

  var lucene = Lucene()

  var configDir: String? = null

  var kobo = Kobo()

  @Positive
  @Deprecated("Artemis has been replaced")
  var taskConsumers: Int = 1

  @Positive
  @Deprecated("Artemis has been replaced")
  var taskConsumersMax: Int = 1

  @Deprecated("Moved to server settings since 1.5.0")
  class RememberMe {
    @Deprecated("Moved to server settings since 1.5.0")
    @get:NotBlank
    var key: String? = null

    @Deprecated("Moved to server settings since 1.5.0")
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

    @DurationUnit(ChronoUnit.SECONDS)
    var commitDelay: Duration = Duration.ofSeconds(2)

    class IndexAnalyzer {
      @get:Positive
      var minGram: Int = 3

      @get:Positive
      var maxGram: Int = 10

      var preserveOriginal: Boolean = true
    }
  }

  class Kobo {
    @get:Positive
    var syncItemLimit: Int = 100
  }
}
