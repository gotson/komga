package org.gotson.komga.interfaces.api.kobo.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.gotson.komga.domain.model.SyncPoint
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class BookEntitlementDto(
  val accessibility: String = "Full",
  val activePeriod: PeriodDto,
  val created: ZonedDateTime,
  val crossRevisionId: String,
  val id: String,
  val isHiddenFromArchive: Boolean = false,
  val isLocked: Boolean = false,
  /**
   * True if the book has been deleted or is not available
   */
  val isRemoved: Boolean,
  val lastModified: ZonedDateTime,
  val originCategory: String = "Imported",
  val revisionId: String,
  val status: String = "Active",
)

fun SyncPoint.Book.toBookEntitlementDto(isRemoved: Boolean) =
  BookEntitlementDto(
    activePeriod = ZonedDateTime.now(ZoneId.of("Z")).toPeriodDto(),
    created = createdDate,
    crossRevisionId = bookId,
    revisionId = bookId,
    id = bookId,
    isRemoved = isRemoved,
    lastModified = fileLastModified,
  )
