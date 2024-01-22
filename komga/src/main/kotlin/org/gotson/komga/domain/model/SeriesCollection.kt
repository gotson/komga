package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.time.LocalDateTime

data class SeriesCollection(
  val name: String,
  /**
   * Indicates whether the collection is ordered manually
   */
  val ordered: Boolean = false,
  val seriesIds: List<String> = emptyList(),
  val id: String = TsidCreator.getTsid256().toString(),
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
  /**
   * Indicates that the seriesIds have been filtered and is not exhaustive.
   */
  val filtered: Boolean = false,
) : Auditable
