package org.gotson.komga.infrastructure.mediacontainer.divina

import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaUnsupportedException
import java.nio.file.Path

interface DivinaExtractor {
  fun mediaTypes(): List<String>

  @Throws(MediaUnsupportedException::class)
  fun getEntries(
    path: Path,
    analyzeDimensions: Boolean,
  ): List<MediaContainerEntry>

  fun getEntryStream(
    path: Path,
    entryName: String,
  ): ByteArray
}
