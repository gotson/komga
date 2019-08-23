package org.gotson.komga.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "komga")
@Validated
class KomgaProperties {
  var rootFolder: String = ""
  var rootFolderScanCron: String = ""

  @NotBlank
  var userPassword: String = "user"

  @NotBlank
  var adminPassword: String = "admin"

  var threads = Threads()

  class Threads {
    @Min(1)
    var parse: Int = 2
  }
}