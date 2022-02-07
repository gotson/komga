package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.PageHash
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.model.PageHashMatch
import org.gotson.komga.domain.model.PageHashUnknown
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PageHashRepository {
  fun findKnown(pageHash: PageHash): PageHashKnown?
  fun findAllKnown(actions: List<PageHashKnown.Action>?, pageable: Pageable): Page<PageHashKnown>
  fun findAllUnknown(pageable: Pageable): Page<PageHashUnknown>

  fun findMatchesByHash(pageHash: PageHash, libraryId: String?, pageable: Pageable): Page<PageHashMatch>

  fun getKnownThumbnail(pageHash: PageHash): ByteArray?

  fun insert(pageHash: PageHashKnown, thumbnail: ByteArray?)
  fun update(pageHash: PageHashKnown)
}
