package org.gotson.komga.domain.persistence

import org.gotson.komga.domain.model.Sidecar
import org.gotson.komga.domain.model.SidecarStored
import java.net.URL

interface SidecarRepository {
  fun findAll(): Collection<SidecarStored>

  fun save(
    libraryId: String,
    sidecar: Sidecar,
  )

  fun deleteByLibraryIdAndUrls(
    libraryId: String,
    urls: Collection<URL>,
  )

  fun deleteByLibraryId(libraryId: String)

  fun countGroupedByLibraryId(): Map<String, Int>
}
