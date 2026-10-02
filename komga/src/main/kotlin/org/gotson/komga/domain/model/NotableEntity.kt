package org.gotson.komga.domain.model

class NotableEntity(
  kind: String,
  name: String,
) {
  val kind = kind.trim().lowercase()
  val name = name.trim()

  override fun toString(): String = "NotableEntity($kind, $name)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NotableEntity) return false

    if (name != other.name) return false
    if (kind != other.kind) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + kind.hashCode()
    return result
  }
}
