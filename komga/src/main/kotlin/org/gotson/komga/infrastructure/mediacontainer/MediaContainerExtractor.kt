package org.gotson.komga.infrastructure.mediacontainer

import org.gotson.komga.domain.model.MediaContainerEntry
import java.nio.file.Path

interface MediaContainerExtractor {
  fun mediaTypes(): List<String>
  fun getEntries(path: Path): List<MediaContainerEntry>
  fun getEntryStream(path: Path, entryName: String): ByteArray
}
