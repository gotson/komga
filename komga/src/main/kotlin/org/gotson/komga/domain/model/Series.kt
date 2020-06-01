package org.gotson.komga.domain.model

import java.net.URL
import java.time.LocalDateTime

data class Series(
  val name: String,
  val url: URL,
  var fileLastModified: LocalDateTime,

  val id: Long = 0,
  val libraryId: Long = 0,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable()
