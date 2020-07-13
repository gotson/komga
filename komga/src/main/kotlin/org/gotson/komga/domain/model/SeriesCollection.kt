package org.gotson.komga.domain.model

import java.time.LocalDateTime

data class SeriesCollection(
  val name: String,
  val ordered: Boolean = false,

  val seriesIds: List<String> = emptyList(),

  val id: Long = 0,

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now(),

  /**
   * Indicates that the seriesIds have been filtered and is not exhaustive.
   */
  val filtered: Boolean = false
) : Auditable()
