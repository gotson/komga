package org.gotson.komga.domain.model

import com.github.f4b6a3.tsid.TsidCreator
import java.time.LocalDateTime

data class ApiKey(
  val id: String = TsidCreator.getTsid256().toString(),
  val userId: String,
  val key: String,
  val comment: String,
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable
