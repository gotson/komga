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
  ;

  companion object {
    fun fromMediaType(mediaType: String?): MediaType? = entries.firstOrNull { it.type == mediaType }

    fun matchingMediaProfile(mediaProfile: MediaProfile): Collection<MediaType> = entries.filter { it.profile == mediaProfile }
  }
}
