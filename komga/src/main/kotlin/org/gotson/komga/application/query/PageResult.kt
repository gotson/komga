package org.gotson.komga.application.query

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import kotlin.math.ceil

data class PageResult<T>(
  val items: List<T>,
  val totalElements: Long,
)

fun <T, R> PageResult<T>.toPageResponse(
  pageable: Pageable,
  transform: (T) -> R,
): PageResponse<R> =
  PageResponse(
    content = items.map(transform),
    page = if (pageable.isUnpaged) 0 else pageable.pageNumber,
    size = if (pageable.isUnpaged) items.size else pageable.pageSize,
    totalElements = totalElements,
    totalPages =
      when {
        totalElements == 0L -> 0
        pageable.isUnpaged -> 1
        else -> ceil(totalElements.toDouble() / pageable.pageSize).toInt()
      },
  )

fun <T> PageResult<T>.toPageResponse(pageable: Pageable): PageResponse<T> = toPageResponse(pageable) { it }

fun <T> Page<T>.toPageResult(): PageResult<T> = PageResult(content, totalElements)
