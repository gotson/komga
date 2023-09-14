package org.gotson.komga.infrastructure.mediacontainer

import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.springframework.stereotype.Service
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Service
class ZipExtractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
) : MediaContainerExtractor {

  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf(MediaType.ZIP.type)

  override fun getEntries(path: Path, analyzeDimensions: Boolean): List<MediaContainerEntry> =
    ZipFile(path.toFile()).use { zip ->
      zip.entries.toList()
        .filter { !it.isDirectory }
        .map { entry ->
          try {
            zip.getInputStream(entry).buffered().use { stream ->
              val mediaType = contentDetector.detectMediaType(stream)
              val dimension = if (analyzeDimensions && contentDetector.isImage(mediaType))
                imageAnalyzer.getDimension(stream)
              else
                null
              val fileSize = if (entry.size == ArchiveEntry.SIZE_UNKNOWN) null else entry.size
              MediaContainerEntry(name = entry.name, mediaType = mediaType, dimension = dimension, fileSize = fileSize)
            }
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${entry.name}" }
            MediaContainerEntry(name = entry.name, comment = e.message)
          }
        }
        .sortedWith(compareBy(natSortComparator) { it.name })
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    ZipFile(path.toFile()).use { zip ->
      zip.getInputStream(zip.getEntry(entryName)).use { it.readBytes() }
    }
}
