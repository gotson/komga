package org.gotson.komga.infrastructure.mediacontainer

import com.github.junrar.Archive
import mu.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.apache.commons.io.input.TeeInputStream
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaUnsupportedException
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.Comparator

private val logger = KotlinLogging.logger {}

@Service
class RarExtractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer
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
        .map { hd ->
          try {
            val buffer = ByteArrayOutputStream()
            TeeInputStream(rar.getInputStream(hd), buffer).use { tee ->
              val mediaType = contentDetector.detectMediaType(tee)
              val dimension = if (contentDetector.isImage(mediaType))
                imageAnalyzer.getDimension(ByteArrayInputStream(buffer.toByteArray()))
              else
                null
              MediaContainerEntry(name = hd.fileName, mediaType = mediaType, dimension = dimension)
            }
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${hd.fileName}" }
            MediaContainerEntry(name = hd.fileName, comment = e.message)
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
