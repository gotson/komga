package org.gotson.komga.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min

@Component
@ConfigurationProperties(prefix = "komga")
@Validated
class KomgaProperties {
  @get:DeprecatedConfigurationProperty(reason = "As of v0.5.0 Komga supports multiple libraries, which must be created via the API")
  var rootFolder: String = ""

  @get:DeprecatedConfigurationProperty(
      reason = "As of v0.5.0 Komga supports multiple libraries, which must be created via the API",
      replacement = "komga.libraries-scan-cron"
  )
  var rootFolderScanCron: String = ""

  var librariesScanCron: String = ""
    get() {
      if (field.isBlank()) return rootFolderScanCron
      return field
    }

  var librariesScanDirectoryExclusions: List<String> = emptyList()

  @get:DeprecatedConfigurationProperty(reason = "As of v0.7.0 Komga supports user creation via the API/UI")
  var userPassword: String = ""

  @get:DeprecatedConfigurationProperty(reason = "As of v0.7.0 Komga supports user creation via the API/UI")
  var adminPassword: String = ""

  var threads = Threads()

  class Threads {
    @Min(1)
    var parse: Int = 2
  }
}
