package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookDtoRepository {
  fun findAll(search: BookSearch, pageable: Pageable): Page<BookDto>
  fun findByIdOrNull(bookId: Long): BookDto?
  fun findPreviousInSeries(bookId: Long): BookDto?
  fun findNextInSeries(bookId: Long): BookDto?
}
