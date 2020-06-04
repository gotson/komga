package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookDtoRepository {
  fun findAll(search: BookSearchWithReadProgress, userId: Long, pageable: Pageable): Page<BookDto>
  fun findByIdOrNull(bookId: Long, userId: Long): BookDto?
  fun findPreviousInSeries(bookId: Long, userId: Long): BookDto?
  fun findNextInSeries(bookId: Long, userId: Long): BookDto?
}
