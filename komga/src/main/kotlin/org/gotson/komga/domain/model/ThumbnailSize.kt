package org.gotson.komga.domain.model

enum class ThumbnailSize(
  val maxEdge: Int,
) {
  DEFAULT(300),
  MEDIUM(600),
  LARGE(900),
  XLARGE(1200),
}
