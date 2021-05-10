package org.gotson.komga.interfaces.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.gotson.komga.domain.model.ReadList
import java.time.LocalDateTime

data class ReadListDto(
  val id: String,
  val name: String,

  val bookIds: List<String>,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val createdDate: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModifiedDate: LocalDateTime,

  val filtered: Boolean
)

data class ReadListProgressDto(
  val booksCount: Int,
  val booksReadCount: Int,
  val booksUnreadCount: Int,
  val booksInProgressCount: Int,
)

fun ReadList.toDto() =
  ReadListDto(
    id = id,
    name = name,
    bookIds = bookIds.values.toList(),
    createdDate = createdDate.toUTC(),
    lastModifiedDate = lastModifiedDate.toUTC(),
    filtered = filtered
  )
