package org.gotson.komga.domain.model

enum class MediaType(val value: String, val fileExtension: String) {
  ZIP("application/zip", "cbz"),
  RAR_GENERIC("application/x-rar-compressed", "cbr"),
  RAR_4("application/x-rar-compressed; version=4", "cbr"),
  EPUB("application/epub+zip", "epub"),
  PDF("application/pdf", "pdf"),
}
