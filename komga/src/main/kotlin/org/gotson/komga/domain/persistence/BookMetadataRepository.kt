package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookMetadata

interface BookMetadataRepository {
  fun findById(bookId: String): BookMetadata
  fun findByIdOrNull(bookId: String): BookMetadata?
  fun findByIds(bookIds: Collection<String>): Collection<BookMetadata>

  fun findAuthorsByName(search: String): List<String>

  fun insert(metadata: BookMetadata)
  fun insertMany(metadatas: Collection<BookMetadata>)
  fun update(metadata: BookMetadata)
  fun updateMany(metadatas: Collection<BookMetadata>)

  fun delete(bookId: String)
  fun deleteByBookIds(bookIds: Collection<String>)
}
