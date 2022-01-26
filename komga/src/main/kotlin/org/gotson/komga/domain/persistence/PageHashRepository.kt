package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.PageHash
import org.gotson.komga.domain.model.PageHashMatch
import org.gotson.komga.domain.model.PageHashUnknown
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PageHashRepository {
  fun findAllKnown(actions: List<PageHash.Action>?, pageable: Pageable): Page<PageHash>
  fun findAllUnknown(pageable: Pageable): Page<PageHashUnknown>

  fun findMatchesByHash(hash: String, pageable: Pageable): Page<PageHashMatch>

  fun getKnownThumbnail(hash: String): ByteArray?
}
