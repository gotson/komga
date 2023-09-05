package org.gotson.komga.infrastructure.mediacontainer

import org.gotson.komga.domain.model.BookPageContent
import java.nio.file.Path

interface MediaContainerRawExtractor : MediaContainerExtractor {
  fun getRawEntryStream(path: Path, entryName: String): BookPageContent
}
