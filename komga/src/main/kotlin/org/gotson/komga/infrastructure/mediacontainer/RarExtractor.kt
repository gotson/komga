package org.gotson.komga.infrastructure.mediacontainer

import com.github.junrar.Archive
import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.Comparator

private val logger = KotlinLogging.logger {}

@Service
class RarExtractor(
  private val contentDetector: ContentDetector
) : MediaContainerExtractor {

  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf("application/x-rar-compressed", "application/x-rar-compressed; version=4")

  override fun getEntries(path: Path): List<MediaContainerEntry> =
    Archive(path.toFile()).use { rar ->
      if (rar.isPasswordProtected) throw MediaUnsupportedException("Encrypted RAR archives are not supported")
      if (rar.mainHeader.isSolid) throw MediaUnsupportedException("Solid RAR archives are not supported")
      if (rar.mainHeader.isMultiVolume) throw MediaUnsupportedException("Multi-Volume RAR archives are not supported")
      rar.fileHeaders
        .filter { !it.isDirectory }
        .map {
          try {
            MediaContainerEntry(name = it.fileName, mediaType = contentDetector.detectMediaType(rar.getInputStream(it)))
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${it.fileName}" }
            MediaContainerEntry(name = it.fileName, comment = e.message)
          }
        }
        .sortedWith(compareBy(natSortComparator) { it.name })
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    Archive(path.toFile()).use { rar ->
      val header = rar.fileHeaders.find { it.fileName == entryName }
      rar.getInputStream(header).readBytes()
    }
}
