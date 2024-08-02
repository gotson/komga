package org.gotson.komga.interfaces.api.kobo.persistence

import org.gotson.komga.interfaces.api.kobo.dto.KoboBookMetadataDto
import org.springframework.web.util.UriBuilder

interface KoboDtoRepository {
  fun findBookMetadataByIds(
    bookIds: Collection<String>,
    downloadUriBuilder: UriBuilder,
  ): Collection<KoboBookMetadataDto>
}
