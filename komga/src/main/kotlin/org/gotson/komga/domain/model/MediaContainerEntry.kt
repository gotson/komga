package org.gotson.komga.domain.model

data class MediaContainerEntry(
  val name: String,
  val mediaType: String? = null,
  val comment: String? = null,
  val dimension: Dimension? = null,
  val fileSize: Long? = null,
)
