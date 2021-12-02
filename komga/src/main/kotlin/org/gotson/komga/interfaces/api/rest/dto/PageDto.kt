package org.gotson.komga.interfaces.api.rest.dto

data class PageDto(
  val number: Int,
  val fileName: String,
  val mediaType: String,
  val width: Int?,
  val height: Int?
)
