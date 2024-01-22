package org.gotson.komga.infrastructure.sidecar

import org.gotson.komga.domain.model.Sidecar

interface SidecarSeriesConsumer {
  fun getSidecarSeriesType(): Sidecar.Type

  fun getSidecarSeriesFilenames(): List<String>
}
