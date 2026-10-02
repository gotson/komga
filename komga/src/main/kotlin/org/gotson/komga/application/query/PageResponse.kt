package org.gotson.komga.application.query

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Paginated API response container")
data class PageResponse<T>(
  @field:Schema(
    description = "List of items contained in the current page",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  val content: List<T>,
  @field:Schema(
    description = "Zero-based page index (0..N)",
    example = "0",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  val page: Int,
  @field:Schema(
    description = "Number of items requested per page",
    example = "20",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  val size: Int,
  @field:Schema(
    description = "Total number of elements across all pages",
    example = "105",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  val totalElements: Long,
  @field:Schema(
    description = "Total number of calculated pages",
    example = "6",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  val totalPages: Int,
) {
  @get:Schema(
    description = "Indicates if the current page is the first page",
    example = "true",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  @get:JsonProperty("first")
  val isFirst: Boolean
    get() = page == 0

  @get:Schema(
    description = "Indicates if the current page is the last page",
    example = "false",
    requiredMode = Schema.RequiredMode.REQUIRED,
  )
  @get:JsonProperty("last")
  val isLast: Boolean
    get() = page >= totalPages - 1 || totalPages == 0
}
