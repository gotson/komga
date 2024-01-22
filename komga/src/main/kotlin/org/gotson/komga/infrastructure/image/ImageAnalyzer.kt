package org.gotson.komga.infrastructure.image

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.Dimension
import org.springframework.stereotype.Service
import java.io.InputStream
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

@Service
class ImageAnalyzer {
  /**
   * Returns the Dimension of the image contained in the stream.
   * The stream will not be closed, nor marked or reset.
   */
  fun getDimension(stream: InputStream): Dimension? =
    try {
      ImageIO.createImageInputStream(stream).use { fis ->
        val readers = ImageIO.getImageReaders(fis)
        if (readers.hasNext()) {
          val reader = readers.next()
          reader.input = fis
          Dimension(reader.getWidth(0), reader.getHeight(0))
        } else {
          logger.warn { "no reader found" }
          null
        }
      }
    } catch (e: Exception) {
      logger.warn(e) { "Could not get image dimensions" }
      null
    }
}
