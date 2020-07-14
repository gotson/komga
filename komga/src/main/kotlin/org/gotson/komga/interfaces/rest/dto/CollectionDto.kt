package org.gotson.komga.interfaces.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.gotson.komga.domain.model.SeriesCollection
import java.time.LocalDateTime

data class CollectionDto(
  val id: String,
  val name: String,
  val ordered: Boolean,

  val seriesIds: List<String>,

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val createdDate: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModifiedDate: LocalDateTime,

  val filtered: Boolean
)

fun SeriesCollection.toDto() =
  CollectionDto(
    id = id,
    name = name,
    ordered = ordered,
    seriesIds = seriesIds,
    createdDate = createdDate.toUTC(),
    lastModifiedDate = lastModifiedDate.toUTC(),
    filtered = filtered
  )
