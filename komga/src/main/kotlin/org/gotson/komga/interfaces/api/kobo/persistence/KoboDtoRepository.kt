package org.gotson.komga.interfaces.api.kobo.persistence

import org.gotson.komga.interfaces.api.kobo.dto.KoboBookMetadataDto

interface KoboDtoRepository {
  fun findBookMetadataByIds(
    bookIds: Collection<String>,
  ): Collection<KoboBookMetadataDto>
}
