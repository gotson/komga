package org.gotson.komga.infrastructure.mediacontainer

import mu.KotlinLogging
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Service
class ContentDetector(
  private val tika: TikaConfig
) {

  fun detectMediaType(path: Path): String {
    val metadata = Metadata().also {
      it[Metadata.RESOURCE_NAME_KEY] = path.fileName.toString()
    }

    return TikaInputStream.get(path).use {
      val mediaType = tika.detector.detect(it, metadata)
      mediaType.toString()
    }
  }

  fun detectMediaType(stream: InputStream): String =
    stream.use {
      TikaInputStream.get(it).use { tikaStream ->
        val mediaType = tika.detector.detect(tikaStream, Metadata())
        mediaType.toString()
      }
    }

  fun isImage(mediaType: String): Boolean =
    mediaType.startsWith("image/")

  fun mediaTypeToExtension(mediaType: String): String? =
    try {
      tika.mimeRepository.forName(mediaType).extension
    } catch (e: Exception) {
      null
    }
}
