package org.gotson.komga.application.query

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

fun <T> buildPage(
  items: List<T>,
  pageable: Pageable,
  count: Int,
  sort: Sort?,
): PageImpl<T> =
  PageImpl(
    items,
    if (pageable.isPaged)
      PageRequest.of(pageable.pageNumber, pageable.pageSize, sort ?: pageable.sort)
    else
      PageRequest.of(0, maxOf(count, 20), sort ?: pageable.sort),
    count.toLong(),
  )
