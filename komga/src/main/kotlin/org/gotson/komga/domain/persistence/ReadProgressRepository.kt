package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ReadProgress


interface ReadProgressRepository {
  fun findAll(): Collection<ReadProgress>
  fun findByBookIdAndUserId(bookId: Long, userId: Long): ReadProgress?
  fun findByUserId(userId: Long): Collection<ReadProgress>

  fun save(readProgress: ReadProgress)

  fun delete(bookId: Long, userId: Long)
  fun deleteByUserId(userId: Long)
  fun deleteByBookId(bookId: Long)
  fun deleteByBookIds(bookIds: Collection<Long>)
  fun deleteAll()
}
