package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.AlternateTitle

data class AlternateTitleDto(
  val label: String,
  val title: String,
)

fun AlternateTitle.toDto() = AlternateTitleDto(label, title)
