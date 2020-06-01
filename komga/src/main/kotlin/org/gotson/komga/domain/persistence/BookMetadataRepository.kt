package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookMetadata

interface BookMetadataRepository {
  fun findById(bookId: Long): BookMetadata
  fun findByIdOrNull(bookId: Long): BookMetadata?

  fun findAuthorsByName(search: String): List<String>

  fun insert(metadata: BookMetadata): BookMetadata
  fun update(metadata: BookMetadata)

  fun delete(bookId: Long)
}
