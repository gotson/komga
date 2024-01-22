package org.gotson.komga.infrastructure.sidecar

import org.gotson.komga.domain.model.Sidecar

interface SidecarBookConsumer {
  fun getSidecarBookType(): Sidecar.Type

  fun getSidecarBookPrefilter(): List<Regex>

  fun isSidecarBookMatch(
    basename: String,
    sidecar: String,
  ): Boolean
}
