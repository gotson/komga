package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.AlternativeTitle

data class AlternativeTitleDto(
  val title: String,
  val hint: String?,
)

fun AlternativeTitle.toDto() = AlternativeTitleDto(title, hint)
