package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.SyncPoint
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SyncPointLifecycle(
  private val syncPointRepository: SyncPointRepository,
) {
  fun createSyncPoint(
    user: KomgaUser,
    apiKeyId: String?,
    libraryIds: List<String>?,
  ): SyncPoint {
    val authorizedLibraryIds = user.getAuthorizedLibraryIds(libraryIds)
    val syncPoint =
      syncPointRepository.create(
        user,
        apiKeyId,
        BookSearch(
          libraryIds = authorizedLibraryIds,
          mediaStatus = setOf(Media.Status.READY),
          mediaProfile = listOf(MediaProfile.EPUB),
          deleted = false,
        ),
      )

    syncPointRepository.addOnDeck(syncPoint.id, user, authorizedLibraryIds)

    return syncPoint
  }

  /**
   * Retrieve a page of un-synced books and mark them as synced.
   */
  fun takeBooks(
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> =
    syncPointRepository.findBooksById(toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markBooksSynced(toSyncPointId, false, page.content.map { it.bookId }) }

  /**
   * Retrieve a page of un-synced added books and mark them as synced.
   */
  fun takeBooksAdded(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> =
    syncPointRepository.findBooksAdded(fromSyncPointId, toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markBooksSynced(toSyncPointId, false, page.content.map { it.bookId }) }

  /**
   * Retrieve a page of un-synced changed books and mark them as synced.
   */
  fun takeBooksChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> =
    syncPointRepository.findBooksChanged(fromSyncPointId, toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markBooksSynced(toSyncPointId, false, page.content.map { it.bookId }) }

  /**
   * Retrieve a page of un-synced removed books and mark them as synced.
   */
  fun takeBooksRemoved(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> =
    syncPointRepository.findBooksRemoved(fromSyncPointId, toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markBooksSynced(toSyncPointId, true, page.content.map { it.bookId }) }

  /**
   * Retrieve a page of un-synced unchanged books with changed read progress and mark them as synced.
   */
  fun takeBooksReadProgressChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.Book> =
    syncPointRepository.findBooksReadProgressChanged(fromSyncPointId, toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markBooksSynced(toSyncPointId, false, page.content.map { it.bookId }) }

  fun takeReadLists(
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> =
    syncPointRepository.findReadListsById(toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markReadListsSynced(toSyncPointId, false, page.content.map { it.readListId }) }

  fun takeReadListsAdded(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> =
    syncPointRepository.findReadListsAdded(fromSyncPointId, toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markReadListsSynced(toSyncPointId, false, page.content.map { it.readListId }) }

  fun takeReadListsChanged(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> =
    syncPointRepository.findReadListsChanged(fromSyncPointId, toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markReadListsSynced(toSyncPointId, false, page.content.map { it.readListId }) }

  fun takeReadListsRemoved(
    fromSyncPointId: String,
    toSyncPointId: String,
    pageable: Pageable,
  ): Page<SyncPoint.ReadList> =
    syncPointRepository.findReadListsRemoved(fromSyncPointId, toSyncPointId, true, pageable)
      .also { page -> syncPointRepository.markReadListsSynced(toSyncPointId, true, page.content.map { it.readListId }) }
}
