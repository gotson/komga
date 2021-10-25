package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime

data class Sidecar(
  val url: URL,
  val parentUrl: URL,
  val lastModifiedTime: LocalDateTime,
  val type: Type,
  val source: Source,
) {

  enum class Type {
    ARTWORK, METADATA
  }

  enum class Source {
    SERIES, BOOK
  }
}

data class SidecarStored(
  val url: URL,
  val parentUrl: URL,
  val lastModifiedTime: LocalDateTime,
  val libraryId: String,
)
