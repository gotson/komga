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

  var filesystemScannerForceDirectoryModifiedTime: Boolean = false

  var rememberMe = RememberMe()

  var database = Database()

  class RememberMe {
    @get:NotBlank
    var key: String? = null

    @get:Positive
    var validity: Int = 1209600 // 2 weeks
  }

  class Database {
    @get:NotBlank
    var file: String = ""

    var batchSize: Int = 500
  }
}
