package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.SyncPoint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SyncPointRepository {
  fun create(
    user: KomgaUser,
    apiKeyId: String?,
    search: BookSearch,
  ): SyncPoint

  fun findByIdOrNull(syncPointId: String): SyncPoint?

  fun findBooksById(
    syncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun findBooksAdded(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun findBooksRemoved(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun findBooksChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun findBooksReadProgressChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    onlyNotSynced: Boolean,
    pageable: Pageable,
  ): Page<SyncPoint.Book>

  fun markBooksSynced(
    syncPointId: String,
    forRemovedBooks: Boolean,
    bookIds: Collection<String>,
  )

  fun deleteByUserId(userId: String)

  fun deleteByUserIdAndApiKeyIds(
    userId: String,
    apiKeyIds: Collection<String>,
  )

  fun deleteOne(syncPointId: String)

  fun deleteAll()
}
