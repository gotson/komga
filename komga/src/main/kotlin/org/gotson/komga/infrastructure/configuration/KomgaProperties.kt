package org.gotson.komga.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min

@Component
@ConfigurationProperties(prefix = "komga")
@Validated
class KomgaProperties {
  var librariesScanCron: String = ""

  var librariesScanDirectoryExclusions: List<String> = emptyList()

  var threads = Threads()

  class Threads {
    @Min(1)
    var parse: Int = 2
  }
}
