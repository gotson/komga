package org.gotson.komga.infrastructure.mediacontainer.divina

import com.github.junrar.Archive
import io.github.oshai.kotlinlogging.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.springframework.stereotype.Service
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Service
class RarExtractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
) : DivinaExtractor {
  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf(MediaType.RAR_GENERIC.type, MediaType.RAR_4.type)

  override fun getEntries(
    path: Path,
    analyzeDimensions: Boolean,
  ): List<MediaContainerEntry> =
    Archive(path.toFile()).use { rar ->
      if (rar.isPasswordProtected) throw MediaUnsupportedException("Encrypted RAR archives are not supported", "ERR_1002")
      if (rar.mainHeader.isSolid) throw MediaUnsupportedException("Solid RAR archives are not supported", "ERR_1003")
      if (rar.mainHeader.isMultiVolume) throw MediaUnsupportedException("Multi-Volume RAR archives are not supported", "ERR_1004")
      rar.fileHeaders
        .filter { !it.isDirectory }
        .map { entry ->
          try {
            val buffer = rar.getInputStream(entry).use { it.readBytes() }
            val mediaType = buffer.inputStream().use { contentDetector.detectMediaType(it) }
            val dimension =
              if (analyzeDimensions && contentDetector.isImage(mediaType))
                buffer.inputStream().use { imageAnalyzer.getDimension(it) }
              else
                null
            val fileSize = entry.fullUnpackSize
            MediaContainerEntry(name = entry.fileName, mediaType = mediaType, dimension = dimension, fileSize = fileSize)
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${entry.fileName}" }
            MediaContainerEntry(name = entry.fileName, comment = e.message)
          }
        }.sortedWith(compareBy(natSortComparator) { it.name })
    }

  override fun getEntryStream(
    path: Path,
    entryName: String,
  ): ByteArray =
    Archive(path.toFile()).use { rar ->
      val header = rar.fileHeaders.find { it.fileName == entryName }
      rar.getInputStream(header).use { it.readBytes() }
    }
}
