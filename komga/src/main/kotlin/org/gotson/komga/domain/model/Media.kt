package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class Media(
  val status: Status = Status.UNKNOWN,
  val mediaType: String? = null,
  val pages: List<BookPage> = emptyList(),
  val pageCount: Int = pages.size,
  val files: List<MediaFile> = emptyList(),
  val comment: String? = null,
  val extension: MediaExtension? = null,
  val bookId: String = "",
  val epubDivinaCompatible: Boolean = false,
  val epubIsKepub: Boolean = false,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  @delegate:Transient
  val profile: MediaProfile? by lazy { MediaType.fromMediaType(mediaType)?.profile }

  enum class Status {
    UNKNOWN,
    ERROR,
    READY,
    UNSUPPORTED,
    OUTDATED,
  }

  override fun toString(): String = "Media(status=$status, mediaType=$mediaType, comment=$comment, bookId='$bookId', createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
}
