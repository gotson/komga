package org.gotson.komga.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.Location

class DoNotIncludeAotTests : ImportOption {
  override fun includes(location: Location): Boolean {
    return !location.contains("/aotTest")
  }
}
