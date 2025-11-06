package org.gotson.komga.infrastructure.mediacontainer.divina

import io.github.oshai.kotlinlogging.KotlinLogging
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gotson.komga.domain.model.EntryNotFoundException
import org.gotson.komga.domain.model.MediaContainerEntry
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.infrastructure.image.ImageAnalyzer
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.gotson.komga.infrastructure.util.getZipEntryBytes
import org.gotson.komga.infrastructure.util.use
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.streams.asSequence

private val logger = KotlinLogging.logger {}

@Service
class ImagesExtractor(
  private val contentDetector: ContentDetector,
  private val imageAnalyzer: ImageAnalyzer,
) : DivinaExtractor {
  private val natSortComparator: Comparator<String> = CaseInsensitiveSimpleNaturalComparator.getInstance()

  override fun mediaTypes(): List<String> = listOf(MediaType.IMAGES.type)

  override fun getEntries(
    path: Path,
    analyzeDimensions: Boolean,
  ): List<MediaContainerEntry> =
    Files.list(path.parent).use { dirStream ->
      dirStream
        .asSequence()
        .map { filePath ->
          try {
            val mediaType = contentDetector.detectMediaType(filePath)
            if (!contentDetector.isImage(mediaType)) {
              return@map null
            }
            val dimension =
              if (analyzeDimensions) {
                filePath.inputStream().use { imageAnalyzer.getDimension(it) }
              } else {
                null
              }
            val fileSize = filePath.fileSize()
            MediaContainerEntry(
              name = filePath.name,
              mediaType = mediaType,
              dimension = dimension,
              fileSize = fileSize,
            )
          } catch (e: Exception) {
            logger.warn(e) { "Could not analyze entry: ${filePath.name}" }
            MediaContainerEntry(name = filePath.name, comment = e.message)
          }
        }
        .filterNotNull()
        .sortedWith(compareBy(natSortComparator) { it.name })
        .toList()
    }

  override fun getEntryStream(
    path: Path,
    entryName: String,
  ): ByteArray {
    val filePath = path.parent.resolve(entryName)
    if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
      throw EntryNotFoundException("Entry does not exist: $entryName")
    }
    return Files.readAllBytes(filePath)
  }
}
