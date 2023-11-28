package org.gotson.komga.infrastructure.mediacontainer.epub

import java.nio.file.Path

data class ResourceContent(
  val path: Path,
  val content: String,
)
