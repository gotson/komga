package org.gotson.komga.infrastructure.jooq

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class UnpagedSorted(
  private val sort: Sort,
) : Pageable {
  override fun getPageNumber(): Int = throw UnsupportedOperationException()

  override fun hasPrevious(): Boolean = false

  override fun getSort(): Sort = sort

  override fun isPaged(): Boolean = false

  override fun next(): Pageable = this

  override fun getPageSize(): Int = throw UnsupportedOperationException()

  override fun getOffset(): Long = throw UnsupportedOperationException()

  override fun first(): Pageable = this

  override fun withPage(pageNumber: Int): Pageable = this

  override fun previousOrFirst(): Pageable = this
}
