package org.gotson.komga.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.language.toUTC
import java.time.LocalDateTime

data class ReadListDto(
  val id: String,
  val name: String,
  val summary: String,
  val ordered: Boolean,
  val bookIds: List<String>,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val createdDate: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val lastModifiedDate: LocalDateTime,
  val filtered: Boolean,
)

fun ReadList.toDto() =
  ReadListDto(
    id = id,
    name = name,
    summary = summary,
    ordered = ordered,
    bookIds = bookIds.values.toList(),
    createdDate = createdDate.toUTC(),
    lastModifiedDate = lastModifiedDate.toUTC(),
    filtered = filtered,
  )
