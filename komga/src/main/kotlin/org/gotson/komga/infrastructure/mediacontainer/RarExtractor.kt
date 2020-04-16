package org.gotson.komga.infrastructure.mediacontainer

import com.github.junrar.Archive
import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.MediaContainerEntry
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class RarExtractor(
  private val contentDetector: ContentDetector
) : MediaContainerExtractor {

  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf("application/x-rar-compressed", "application/x-rar-compressed; version=4")

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
        .sortedWith(compareBy(natSortComparator) { it.name })
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    Archive(Files.newInputStream(path)).use { rar ->
      val header = rar.fileHeaders.find { it.fileNameString == entryName }
      rar.getInputStream(header).readBytes()
    }
}
