package org.gotson.komga.infrastructure.metadata

import org.gotson.komga.domain.model.Library
import org.gotson.komga.domain.model.MetadataPatchTarget

interface MetadataProvider {
  fun shouldLibraryHandlePatch(
    library: Library,
    target: MetadataPatchTarget,
  ): Boolean
}
