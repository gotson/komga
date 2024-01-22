package org.gotson.komga.domain.model

data class MediaFile(
  val fileName: String,
  val mediaType: String? = null,
  val subType: SubType? = null,
  val fileSize: Long? = null,
) {
  enum class SubType {
    EPUB_PAGE,
    EPUB_ASSET,
  }
}
