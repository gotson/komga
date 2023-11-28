package org.gotson.komga.infrastructure.mediacontainer.epub

data class ManifestItem(
  val id: String,
  val href: String,
  val mediaType: String,
  val properties: Set<String> = emptySet(),
)
