package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookDtoRepository {
  fun findAll(search: BookSearchWithReadProgress, userId: String, pageable: Pageable): Page<BookDto>
  fun findByIdOrNull(bookId: String, userId: String): BookDto?
  fun findPreviousInSeries(bookId: String, userId: String): BookDto?
  fun findNextInSeries(bookId: String, userId: String): BookDto?
  fun findOnDeck(libraryIds: Collection<String>, userId: String, pageable: Pageable): Page<BookDto>
}
