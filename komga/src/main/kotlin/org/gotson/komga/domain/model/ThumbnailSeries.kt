package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.net.URL
import java.time.LocalDateTime

data class ThumbnailSeries(
  val url: URL,
  val selected: Boolean = false,

  val id: String = TsidCreator.getTsidString256(),
  val seriesId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable()
