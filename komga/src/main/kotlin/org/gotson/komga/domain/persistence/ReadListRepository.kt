package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.ReadList
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ReadListRepository {
  /**
   * Find one ReadList by [readListId],
   * optionally with only bookIds filtered by the provided [filterOnLibraryIds] it not null.
   */
  fun findByIdOrNull(
    readListId: String,
    filterOnLibraryIds: Collection<String>? = null,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): ReadList?

  /**
   * Find all ReadList
   * optionally with at least one Book belonging to the provided [belongsToLibraryIds] if not null,
   * optionally with only bookIds filtered by the provided [filterOnLibraryIds] if not null.
   */
  fun findAll(
    belongsToLibraryIds: Collection<String>? = null,
    filterOnLibraryIds: Collection<String>? = null,
    search: String? = null,
    pageable: Pageable,
    restrictions: ContentRestrictions = ContentRestrictions(),
  ): Page<ReadList>

  /**
   * Find all ReadList that contains the provided [containsBookId],
   * optionally with only bookIds filtered by the provided [filterOnLibraryIds] if not null.
   */
  fun findAllContainingBookId(
    containsBookId: String,
    filterOnLibraryIds: Collection<String>?,
    restrictions: ContentRestrictions = ContentRestrictions(),
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
