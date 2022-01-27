package org.gotson.komga.domain.model

data class PageHashUnknown(
  val hash: String,
  val mediaType: String,
  val size: Long? = null,
  val matchCount: Int = 0,
)
