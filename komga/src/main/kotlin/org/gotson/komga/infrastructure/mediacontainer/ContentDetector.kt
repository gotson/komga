package org.gotson.komga.infrastructure.mediacontainer

import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.name

@Service
class ContentDetector(
  private val tika: TikaConfig,
) {
  fun detectMediaType(path: Path): String {
    val metadata =
      Metadata().also {
        it[Metadata.TIKA_MIME_FILE] = path.name
      }

    return TikaInputStream.get(path).use {
      val mediaType = tika.detector.detect(it, metadata)
      mediaType.toString()
    }
  }

  /**
   * Detects the media type of the content of the stream.
   * The stream will not be closed.
   */
  fun detectMediaType(stream: InputStream): String = tika.detector.detect(stream, Metadata()).toString()

  fun isImage(mediaType: String): Boolean = mediaType.startsWith("image/")

  fun mediaTypeToExtension(mediaType: String): String? =
    try {
      tika.mimeRepository.forName(mediaType).extension
    } catch (e: Exception) {
      null
    }
}
