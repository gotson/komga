package org.gotson.komga.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.sqlite.SQLiteConfig.JournalMode
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

  @Deprecated("Deprecated since 0.143.0, you can configure this in the library options directly")
  var fileHashing: Boolean = true

  @Positive
  var pageHashing: Int = 3

  var rememberMe = RememberMe()

  @DurationUnit(ChronoUnit.SECONDS)
  var sessionTimeout: Duration = Duration.ofMinutes(30)

  var oauth2AccountCreation: Boolean = false

  var database = Database()

  var cors = Cors()

  var lucene = Lucene()

  var configDir: String? = null

  @Positive
  var taskConsumers: Int = 1

  @Positive
  var taskConsumersMax: Int = 1

  var PSPDFKit = PSPDFKitConfig()

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

  class PSPDFKitConfig{
    var protocol: String="http"
    var host:String ="localhost"
    var port:Int = 5000
    var token: String = ""
    var publicKey:String=""
    var privateKey:String=""
  }
}
