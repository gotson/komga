package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.ReadProgress

interface ReadProgressRepository {
  fun findByBookIdAndUserIdOrNull(
    bookId: String,
    userId: String,
  ): ReadProgress?

  fun findAll(): Collection<ReadProgress>

  fun findAllByUserId(userId: String): Collection<ReadProgress>

  fun findAllByBookId(bookId: String): Collection<ReadProgress>

  fun findAllByBookIdsAndUserId(
    bookIds: Collection<String>,
    userId: String,
  ): Collection<ReadProgress>

  fun save(readProgress: ReadProgress)

  fun save(readProgresses: Collection<ReadProgress>)

  fun delete(
    bookId: String,
    userId: String,
  )

  fun deleteByUserId(userId: String)

  fun deleteByBookId(bookId: String)

  fun deleteByBookIds(bookIds: Collection<String>)

  fun deleteByBookIdsAndUserId(
    bookIds: Collection<String>,
    userId: String,
  )

  fun deleteBySeriesIds(seriesIds: Collection<String>)

  fun deleteAll()
}
