package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ReadProgress


interface ReadProgressRepository {
  fun findAll(): Collection<ReadProgress>
  fun findByBookIdAndUserId(bookId: String, userId: String): ReadProgress?
  fun findByUserId(userId: String): Collection<ReadProgress>

  fun save(readProgress: ReadProgress)

  fun delete(bookId: String, userId: String)
  fun deleteByUserId(userId: String)
  fun deleteByBookId(bookId: String)
  fun deleteByBookIds(bookIds: Collection<String>)
  fun deleteAll()
}
