package org.gotson.komga.domain.model

class Author(
  name: String,
  role: String
) {
  val name = name.trim()
  val role = role.trim().toLowerCase()

  override fun toString(): String = "Author($name, $role)"
}
