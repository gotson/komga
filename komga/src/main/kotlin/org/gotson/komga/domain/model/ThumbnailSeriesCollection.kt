package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.time.LocalDateTime

data class ThumbnailSeriesCollection(
  val thumbnail: ByteArray,
  val selected: Boolean = false,
  val type: Type,
  val mediaType: String,
  val fileSize: Long,
  val dimension: Dimension,
  val id: String = TsidCreator.getTsid256().toString(),
  val collectionId: String = "",
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  enum class Type {
    USER_UPLOADED,
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ThumbnailSeriesCollection

    if (!thumbnail.contentEquals(other.thumbnail)) return false
    if (selected != other.selected) return false
    if (type != other.type) return false
    if (mediaType != other.mediaType) return false
    if (fileSize != other.fileSize) return false
    if (dimension != other.dimension) return false
    if (id != other.id) return false
    if (collectionId != other.collectionId) return false
    if (createdDate != other.createdDate) return false
    if (lastModifiedDate != other.lastModifiedDate) return false

    return true
  }

  override fun hashCode(): Int {
    var result = thumbnail.contentHashCode()
    result = 31 * result + selected.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + mediaType.hashCode()
    result = 31 * result + fileSize.hashCode()
    result = 31 * result + dimension.hashCode()
    result = 31 * result + id.hashCode()
    result = 31 * result + collectionId.hashCode()
    result = 31 * result + createdDate.hashCode()
    result = 31 * result + lastModifiedDate.hashCode()
    return result
  }
}
