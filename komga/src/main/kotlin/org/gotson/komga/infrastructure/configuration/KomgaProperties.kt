package org.gotson.komga.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
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

  var nativeWebp: Boolean = true

  var database = Database()

  var cors = Cors()

  var lucene = Lucene()

  var customCovers = CustomCovers()

  class RememberMe {
    @get:NotBlank
    var key: String? = null

    @get:Positive
    var validity: Int = 1209600 // 2 weeks
  }

  class Cors {
    var allowedOrigins: List<String> = emptyList()
  }

  class Database {
    @get:NotBlank
    var file: String = ""

    @Deprecated("Unused since 0.81.0")
    var batchSize: Int = 500
  }

  class Lucene {
    @get:NotBlank
    var dataDirectory: String = ""
  }

  class CustomCovers {
    @get:NotBlank
    var dataDirectory: String = ""
  }
}
