package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.WebLink

data class WebLinkDto(
  val label: String,
  val url: String,
)

fun WebLink.toDto() = WebLinkDto(label, url.toString())
