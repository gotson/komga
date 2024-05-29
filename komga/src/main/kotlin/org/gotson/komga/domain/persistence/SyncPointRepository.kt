package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.SyncPoint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SyncPointRepository {
  fun create(
    user: KomgaUser,
    search: BookSearch,
  ): SyncPoint

  fun findByIdOrNull(syncPointId: String): SyncPoint?

  fun findBooksById(
    syncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun findBooksAdded(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun findBooksRemoved(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun findBooksChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun deleteByUserId(userId: String)

  fun deleteAll()
}
