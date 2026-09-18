package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ReadList
import org.gotson.komga.domain.model.SearchContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ReadListRepository {
  /**
   * Find one ReadList by [readListId],
   * bookIds will be filtered by the provided [context] libraries.
   */
  fun findByIdOrNull(
    readListId: String,
    context: SearchContext,
  ): ReadList?

  /**
   * Find all ReadList
   * optionally with at least one Book belonging to the provided [belongsToLibraryIds] if not null,
   * bookIds will be filtered by the provided [context] libraries.
   */
  fun findAll(
    context: SearchContext,
    pageable: Pageable,
    belongsToLibraryIds: Collection<String>? = null,
    search: String? = null,
  ): Page<ReadList>

  /**
   * Find all ReadList that contains the provided [containsBookId],
   * bookIds will be filtered by the provided [context] libraries.
   */
  fun findAllContainingBookId(
    containsBookId: String,
    context: SearchContext,
  ): Collection<ReadList>

  fun findAllEmpty(): Collection<ReadList>

  fun findByNameOrNull(name: String): ReadList?

  fun insert(readList: ReadList)

  fun update(readList: ReadList)

  fun removeBookFromAll(bookId: String)

  fun removeBooksFromAll(bookIds: Collection<String>)

  fun delete(readListId: String)

  fun delete(readListIds: Collection<String>)

  fun deleteAll()

  fun existsByName(name: String): Boolean

  fun count(): Long
}
