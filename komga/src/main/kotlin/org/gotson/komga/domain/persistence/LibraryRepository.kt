package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Library

interface LibraryRepository {
  fun findByIdOrNull(libraryId: String): Library?
  fun findById(libraryId: String): Library
  fun findAll(): Collection<Library>
  fun findAllById(libraryIds: Collection<String>): Collection<Library>

  fun delete(libraryId: String)
  fun deleteAll()

  fun insert(library: Library)
  fun update(library: Library)

  fun count(): Long
}
