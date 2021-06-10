package org.gotson.komga.domain.model

data class ScanResult(
  val series: Map<Series, List<Book>>,
  val sidecars: List<Sidecar>,
)
