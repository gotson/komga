package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.io.Serializable
import java.time.LocalDateTime

data class ThumbnailReadList(
  val thumbnail: ByteArray,
  val selected: Boolean = false,
  val type: Type,

  val id: String = TsidCreator.getTsid256().toString(),
  val readListId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable, Serializable {
  enum class Type {
    USER_UPLOADED
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ThumbnailReadList) return false

    if (!thumbnail.contentEquals(other.thumbnail)) return false
    if (selected != other.selected) return false
    if (type != other.type) return false
    if (id != other.id) return false
    if (readListId != other.readListId) return false
    if (createdDate != other.createdDate) return false
    if (lastModifiedDate != other.lastModifiedDate) return false

    return true
  }

  override fun hashCode(): Int {
    var result = thumbnail.contentHashCode()
    result = 31 * result + selected.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + id.hashCode()
    result = 31 * result + readListId.hashCode()
    result = 31 * result + createdDate.hashCode()
    result = 31 * result + lastModifiedDate.hashCode()
    return result
  }
}
