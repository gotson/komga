package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.io.Serializable
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime

data class ThumbnailSeries(
  val thumbnail: ByteArray? = null,
  val url: URL? = null,
  val selected: Boolean = false,
  val type: Type,

  val id: String = TsidCreator.getTsid256().toString(),
  val seriesId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable, Serializable {
  enum class Type {
    SIDECAR, USER_UPLOADED
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ThumbnailSeries) return false

    if (thumbnail != null) {
      if (other.thumbnail == null) return false
      if (!thumbnail.contentEquals(other.thumbnail)) return false
    } else if (other.thumbnail != null) return false
    if (url != other.url) return false
    if (selected != other.selected) return false
    if (type != other.type) return false
    if (id != other.id) return false
    if (seriesId != other.seriesId) return false
    if (createdDate != other.createdDate) return false
    if (lastModifiedDate != other.lastModifiedDate) return false

    return true
  }

  override fun hashCode(): Int {
    var result = thumbnail?.contentHashCode() ?: 0
    result = 31 * result + (url?.hashCode() ?: 0)
    result = 31 * result + selected.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + id.hashCode()
    result = 31 * result + seriesId.hashCode()
    result = 31 * result + createdDate.hashCode()
    result = 31 * result + lastModifiedDate.hashCode()
    return result
  }

  fun exists(): Boolean {
    if (url != null) return Files.exists(Paths.get(url.toURI()))
    return thumbnail != null
  }
}
