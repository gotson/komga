package org.gotson.komga.infrastructure.mediacontainer

import mu.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.MediaContainerEntry
import org.springframework.stereotype.Service
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Service
class ZipExtractor(
  private val contentDetector: ContentDetector
) : MediaContainerExtractor() {

  override fun getEntries(path: Path): List<MediaContainerEntry> =
    ZipFile(path.toFile()).use { zip ->
      zip.entries.toList()
        .filter { !it.isDirectory }
        .map {
          try {
            MediaContainerEntry(name = it.name, mediaType = contentDetector.detectMediaType(zip.getInputStream(it)))
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${it.name}" }
            MediaContainerEntry(name = it.name, comment = e.message)
          }
        }
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    ZipFile(path.toFile()).use {
      it.getInputStream(it.getEntry(entryName)).readBytes()
    }
}
