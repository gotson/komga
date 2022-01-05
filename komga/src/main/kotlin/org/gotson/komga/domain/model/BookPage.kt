package org.gotson.komga.domain.model

data class BookPage(
  val fileName: String,
  val mediaType: String,
  val dimension: Dimension? = null,
)
