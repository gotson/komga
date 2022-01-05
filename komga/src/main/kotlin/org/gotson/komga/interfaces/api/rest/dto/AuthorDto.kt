package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.Author

data class AuthorDto(
  val name: String,
  val role: String,
)

fun Author.toDto() = AuthorDto(name, role)
