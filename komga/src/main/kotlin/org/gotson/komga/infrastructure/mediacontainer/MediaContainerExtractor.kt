package org.gotson.komga.infrastructure.mediacontainer

import org.gotson.komga.domain.model.MediaContainerEntry
import java.nio.file.Path

abstract class MediaContainerExtractor {
  abstract fun getEntries(path: Path): List<MediaContainerEntry>
  abstract fun getEntryStream(path: Path, entryName: String): ByteArray
}
