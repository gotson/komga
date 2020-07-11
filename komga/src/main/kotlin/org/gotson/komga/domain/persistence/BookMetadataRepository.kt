package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookMetadata

interface BookMetadataRepository {
  fun findById(bookId: Long): BookMetadata
  fun findByIdOrNull(bookId: Long): BookMetadata?
  fun findByIds(bookIds: Collection<Long>): Collection<BookMetadata>

  fun findAuthorsByName(search: String): List<String>

  fun insert(metadata: BookMetadata): BookMetadata
  fun insertMany(metadatas: Collection<BookMetadata>): Collection<BookMetadata>
  fun update(metadata: BookMetadata)
  fun updateMany(metadatas: Collection<BookMetadata>)

  fun delete(bookId: Long)
  fun deleteByBookIds(bookIds: Collection<Long>)
}
