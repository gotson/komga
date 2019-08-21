package org.gotson.komga.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "komga")
class KomgaProperties {
  var rootFolder: String = ""
  var rootFolderScanCron: String = ""
  var userPassword: String = "user"
  var adminPassword: String = "admin"
}

