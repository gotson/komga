package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Library

interface LibraryRepository {
  fun findById(libraryId: String): Library

  fun findByIdOrNull(libraryId: String): Library?

  fun findAll(): Collection<Library>

  fun findAllByIds(libraryIds: Collection<String>): Collection<Library>

  fun delete(libraryId: String)

  fun deleteAll()

  fun insert(library: Library)

  fun update(library: Library)

  fun count(): Long
}
