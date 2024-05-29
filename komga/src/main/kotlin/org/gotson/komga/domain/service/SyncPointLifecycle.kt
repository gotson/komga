package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.BookSearch
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.Media
import org.gotson.komga.domain.model.MediaProfile
import org.gotson.komga.domain.model.SyncPoint
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.springframework.stereotype.Component

@Component
class SyncPointLifecycle(
  private val syncPointRepository: SyncPointRepository,
) {
  fun createSyncPoint(
    user: KomgaUser,
    libraryIds: List<String>?,
  ): SyncPoint =
    syncPointRepository.create(
      user,
      BookSearch(
        libraryIds = user.getAuthorizedLibraryIds(libraryIds),
        mediaStatus = setOf(Media.Status.READY),
        mediaProfile = listOf(MediaProfile.EPUB),
        deleted = false,
      ),
    )
}
