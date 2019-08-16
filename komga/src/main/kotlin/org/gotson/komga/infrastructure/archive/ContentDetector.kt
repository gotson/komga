package org.gotson.komga.infrastructure.archive

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
    logger.info { "detect media type for path: $path" }

    val metadata = Metadata().also {
      it[Metadata.RESOURCE_NAME_KEY] = path.fileName.toString()
    }
    val mediaType = tika.detector.detect(TikaInputStream.get(path), metadata)

    logger.info { "media type detected: $mediaType" }

    return mediaType.toString()
  }

  fun detectMediaType(stream: InputStream): String {
    logger.info { "detect media type for stream" }
    stream.use {
      val mediaType = tika.detector.detect(TikaInputStream.get(it), Metadata())
      logger.info { "media type detected: $mediaType" }
      return mediaType.toString()
    }
  }
}