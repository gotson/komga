package org.gotson.komga.interfaces.rest.persistence

import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.interfaces.rest.dto.BookDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookDtoRepository {
  fun findAll(search: BookSearchWithReadProgress, userId: String, pageable: Pageable): Page<BookDto>

  /**
   * Find books that are part of a readlist, optionally filtered by library
   */
  fun findByReadListId(
    readListId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable
  ): Page<BookDto>

  fun findByIdOrNull(bookId: String, userId: String): BookDto?

  fun findPreviousInSeries(bookId: String, userId: String): BookDto?
  fun findNextInSeries(bookId: String, userId: String): BookDto?

  fun findPreviousInReadList(
    readListId: String,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?
  ): BookDto?

  fun findNextInReadList(
    readListId: String,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?
  ): BookDto?

  fun findOnDeck(libraryIds: Collection<String>, userId: String, pageable: Pageable): Page<BookDto>
}
