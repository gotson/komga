package org.gotson.komga.domain.model

open class PageHash(
  val hash: String,
  size: Long? = null,
) {
  val size: Long? = if (size != null && size < 0) null else size
}
