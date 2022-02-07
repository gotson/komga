package org.gotson.komga.domain.model

class PageHashUnknown(
  hash: String,
  mediaType: String,
  size: Long? = null,
  val matchCount: Int = 0,
) : PageHash(hash, mediaType, size)
