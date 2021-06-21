package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.io.Serializable
import java.net.URL
import java.time.LocalDateTime

data class ThumbnailSeries(
  val url: URL,
  val selected: Boolean = false,

  val id: String = TsidCreator.getTsid256().toString(),
  val seriesId: String = "",

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now()
) : Auditable(), Serializable
