package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.time.LocalDateTime

data class Series(
  val name: String,
  val url: URL,
  val fileLastModified: LocalDateTime,

  val id: String = TsidCreator.getTsidString256(),
  val libraryId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable()
