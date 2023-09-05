package org.gotson.komga.domain.model

enum class MediaType(val type: String, val fileExtension: String, val exportType: String = type) {
  ZIP("application/zip", "cbz", "application/vnd.comicbook+zip"),
  RAR_GENERIC("application/x-rar-compressed", "cbr", "application/vnd.comicbook-rar"),
  RAR_4("application/x-rar-compressed; version=4", "cbr", "application/vnd.comicbook-rar"),
  EPUB("application/epub+zip", "epub"),
  PDF("application/pdf", "pdf"),
  ;

  companion object {
    fun fromMediaType(mediaType: String?): MediaType? = values().firstOrNull { it.type == mediaType }
  }
}
