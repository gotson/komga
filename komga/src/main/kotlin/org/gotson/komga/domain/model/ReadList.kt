package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.io.Serializable
import java.time.LocalDateTime
import java.util.SortedMap

data class ReadList(
  val name: String,

  val bookIds: SortedMap<Int, String> = sortedMapOf(),

  val id: String = TsidCreator.getTsid256().toString(),

  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = LocalDateTime.now(),

  /**
   * Indicates that the bookIds have been filtered and is not exhaustive.
   */
  val filtered: Boolean = false
) : Auditable(), Serializable
