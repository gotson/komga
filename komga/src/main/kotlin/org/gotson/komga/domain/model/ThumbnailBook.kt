package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime

data class ThumbnailBook(
  val thumbnail: ByteArray? = null,
  val url: URL? = null,
  val selected: Boolean = false,
  val type: Type,
  val mediaType: String,
  val fileSize: Long,
  val dimension: Dimension,
  val id: String = TsidCreator.getTsid256().toString(),
  val bookId: String = "",
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  enum class Type {
    GENERATED,
    SIDECAR,
    USER_UPLOADED,
  }

  fun exists(): Boolean {
    if (url != null) return Files.exists(Paths.get(url.toURI()))
    return thumbnail != null
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ThumbnailBook

    if (thumbnail != null) {
      if (other.thumbnail == null) return false
      if (!thumbnail.contentEquals(other.thumbnail)) return false
    } else if (other.thumbnail != null)
      return false
    if (url != other.url) return false
    if (selected != other.selected) return false
    if (type != other.type) return false
    if (mediaType != other.mediaType) return false
    if (fileSize != other.fileSize) return false
    if (dimension != other.dimension) return false
    if (id != other.id) return false
    if (bookId != other.bookId) return false
    if (createdDate != other.createdDate) return false
    if (lastModifiedDate != other.lastModifiedDate) return false

    return true
  }

  override fun hashCode(): Int {
    var result = thumbnail?.contentHashCode() ?: 0
    result = 31 * result + (url?.hashCode() ?: 0)
    result = 31 * result + selected.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + mediaType.hashCode()
    result = 31 * result + fileSize.hashCode()
    result = 31 * result + dimension.hashCode()
    result = 31 * result + id.hashCode()
    result = 31 * result + bookId.hashCode()
    result = 31 * result + createdDate.hashCode()
    result = 31 * result + lastModifiedDate.hashCode()
    return result
  }
}
