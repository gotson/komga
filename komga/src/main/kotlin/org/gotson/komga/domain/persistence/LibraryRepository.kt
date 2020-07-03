package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Library

interface LibraryRepository {
  fun findByIdOrNull(libraryId: Long): Library?
  fun findById(libraryId: Long): Library
  fun findAll(): Collection<Library>
  fun findAllById(libraryIds: Collection<Long>): Collection<Library>

  fun delete(libraryId: Long)
  fun deleteAll()

  fun insert(library: Library): Library
  fun update(library: Library)

  fun count(): Long
}
