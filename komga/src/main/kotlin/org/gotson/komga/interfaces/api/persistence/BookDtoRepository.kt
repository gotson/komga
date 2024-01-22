package org.gotson.komga.interfaces.api.persistence

import org.gotson.komga.domain.model.BookSearchWithReadProgress
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.interfaces.api.rest.dto.BookDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookDtoRepository {
  fun findAll(
    search: BookSearchWithReadProgress,
    userId: String,
    pageable: Pageable,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): Page<BookDto>

  /**
   * Find books that are part of a readlist, optionally filtered by library
   */
  fun findAllByReadListId(
    readListId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    search: BookSearchWithReadProgress,
    pageable: Pageable,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): Page<BookDto>

  fun findByIdOrNull(
    bookId: String,
    userId: String,
  ): BookDto?

  fun findPreviousInSeriesOrNull(
    bookId: String,
    userId: String,
  ): BookDto?

  fun findNextInSeriesOrNull(
    bookId: String,
    userId: String,
  ): BookDto?

  fun findPreviousInReadListOrNull(
    readList: ReadList,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): BookDto?

  fun findNextInReadListOrNull(
    readList: ReadList,
    bookId: String,
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): BookDto?

  fun findAllOnDeck(
    userId: String,
    filterOnLibraryIds: Collection<String>?,
    pageable: Pageable,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): Page<BookDto>

  fun findAllDuplicates(
    userId: String,
    pageable: Pageable,
  ): Page<BookDto>
}
