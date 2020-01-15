package org.gotson.komga.infrastructure.mediacontainer

import com.github.junrar.Archive
import mu.KotlinLogging
import org.gotson.komga.domain.model.MediaContainerEntry
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Service
class RarExtractor(
  private val contentDetector: ContentDetector
) : MediaContainerExtractor() {

  override fun getEntries(path: Path): List<MediaContainerEntry> =
    Archive(Files.newInputStream(path)).use { rar ->
      rar.fileHeaders
        .filter { !it.isDirectory }
        .map {
          try {
            MediaContainerEntry(name = it.fileNameString, mediaType = contentDetector.detectMediaType(rar.getInputStream(it)))
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${it.fileNameString}" }
            MediaContainerEntry(name = it.fileNameString, comment = e.message)
          }
        }
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    Archive(Files.newInputStream(path)).use { rar ->
      val header = rar.fileHeaders.find { it.fileNameString == entryName }
      rar.getInputStream(header).readBytes()
    }
}
