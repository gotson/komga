package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.BookPageNumbered
import org.gotson.komga.domain.model.PageHashKnown
import org.gotson.komga.domain.model.PageHashMatch
import org.gotson.komga.domain.model.PageHashUnknown
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PageHashRepository {
  fun findKnown(pageHash: String): PageHashKnown?

  fun findAllKnown(
    actions: List<PageHashKnown.Action>?,
    pageable: Pageable,
  ): Page<PageHashKnown>

  fun findAllUnknown(pageable: Pageable): Page<PageHashUnknown>

  fun findMatchesByHash(
    pageHash: String,
    pageable: Pageable,
  ): Page<PageHashMatch>

  fun findMatchesByKnownHashAction(
    actions: List<PageHashKnown.Action>?,
    libraryId: String?,
  ): Map<String, Collection<BookPageNumbered>>

  fun getKnownThumbnail(pageHash: String): ByteArray?

  fun insert(
    pageHash: PageHashKnown,
    thumbnail: ByteArray?,
  )

  fun update(pageHash: PageHashKnown)
}
