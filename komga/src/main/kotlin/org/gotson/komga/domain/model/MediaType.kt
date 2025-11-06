package org.gotson.komga.domain.model

enum class MediaType(
  val type: String,
  val profile: MediaProfile,
  val fileExtension: String,
  val exportType: String = type,
) {
  ZIP("application/zip", MediaProfile.DIVINA, "cbz", "application/vnd.comicbook+zip"),
  RAR_GENERIC("application/x-rar-compressed", MediaProfile.DIVINA, "cbr", "application/vnd.comicbook-rar"),
  RAR_4("application/x-rar-compressed; version=4", MediaProfile.DIVINA, "cbr", "application/vnd.comicbook-rar"),
  RAR_5("application/x-rar-compressed; version=5", MediaProfile.DIVINA, "cbr", "application/vnd.comicbook-rar"),
  EPUB("application/epub+zip", MediaProfile.EPUB, "epub"),
  PDF("application/pdf", MediaProfile.PDF, "pdf"),
  IMAGES("application/komga-images", MediaProfile.DIVINA, "komga_images"),
  ;

  companion object {
    fun fromMediaType(mediaType: String?, ext: String? = null): MediaType? = entries.firstOrNull {
      if (mediaType == "application/octet-stream" && !ext.isNullOrBlank()) {
        ext.lowercase() == it.fileExtension
      } else it.type == mediaType
    }

    fun matchingMediaProfile(mediaProfile: MediaProfile): Collection<MediaType> = entries.filter { it.profile == mediaProfile }
  }
}
