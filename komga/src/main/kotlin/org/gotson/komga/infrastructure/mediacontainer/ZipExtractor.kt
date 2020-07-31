package org.gotson.komga.infrastructure.mediacontainer

import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.Comparator

private val logger = KotlinLogging.logger {}

@Service
class ZipExtractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer
) : MediaContainerExtractor {

  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf("application/zip")

  override fun getEntries(path: Path): List<MediaContainerEntry> =
    ZipFile(path.toFile()).use { zip ->
      zip.entries.toList()
        .filter { !it.isDirectory }
        .map {
          try {
            val mediaType = contentDetector.detectMediaType(zip.getInputStream(it))
            val dimension = if (contentDetector.isImage(mediaType))
              imageAnalyzer.getDimension(zip.getInputStream(it))
            else
              null
            MediaContainerEntry(name = it.name, mediaType = mediaType, dimension = dimension)
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${it.name}" }
            MediaContainerEntry(name = it.name, comment = e.message)
          }
        }
        .sortedWith(compareBy(natSortComparator) { it.name })
    }

  override fun getEntryStream(path: Path, entryName: String): ByteArray =
    ZipFile(path.toFile()).use {
      it.getInputStream(it.getEntry(entryName)).readBytes()
    }
}
