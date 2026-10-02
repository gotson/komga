package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.NotableEntity

data class NotableEntityDto(
  val kind: String,
  val name: String,
)

fun NotableEntity.toDto() = NotableEntityDto(kind, name)
