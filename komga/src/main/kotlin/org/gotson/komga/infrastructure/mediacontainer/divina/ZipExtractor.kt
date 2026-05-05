package org.gotson.komga.infrastructure.mediacontainer.divina

import io.github.oshai.kotlinlogging.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.mediacontainer.MediaFileDecryptionService
import org.gotson.komga.infrastructure.util.use
import org.springframework.stereotype.Service
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Service
class ZipExtractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
  private val mediaFileDecryptionService: MediaFileDecryptionService,
) : DivinaExtractor {
  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf(MediaType.ZIP.type)

  override fun getEntries(
    path: Path,
    analyzeDimensions: Boolean,
  ): List<MediaContainerEntry> = getEntriesFromZip(path, analyzeDimensions)

  private fun getEntriesFromZip(
    path: Path,
    analyzeDimensions: Boolean,
  ): List<MediaContainerEntry> =
    ZipFile.builder().setPath(path).use { zip ->
      zip.entries
        .toList()
        .filter { !it.isDirectory }
        .map { entry ->
          try {
            val bytes = mediaFileDecryptionService.getZipEntryBytes(path, entry.name)
            val mediaType = bytes.inputStream().use { contentDetector.detectMediaType(it) }
            val dimension =
              if (analyzeDimensions && contentDetector.isImage(mediaType))
                bytes.inputStream().use { imageAnalyzer.getDimension(it) }
              else
                null
            MediaContainerEntry(name = entry.name, mediaType = mediaType, dimension = dimension, fileSize = bytes.size.toLong())
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${entry.name}" }
            MediaContainerEntry(name = entry.name, comment = e.message)
          }
        }.sortedWith(compareBy(natSortComparator) { it.name })
    }

  override fun getEntryStream(
    path: Path,
    entryName: String,
  ): ByteArray = mediaFileDecryptionService.getZipEntryBytes(path, entryName)
}
