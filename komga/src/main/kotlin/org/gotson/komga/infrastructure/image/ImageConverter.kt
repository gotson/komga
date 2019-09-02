package org.gotson.komga.infrastructure.image

import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

@Service
class ImageConverter {

  val supportedReadFormats = ImageIO.getReaderFormatNames().toList()
  val supportedWriteFormats = ImageIO.getWriterFormatNames().toList()

  init {
    logger.info { "Supported read formats: $supportedReadFormats" }
    logger.info { "Supported write formats: $supportedWriteFormats" }
  }

  fun canConvert(from: String, to: String) =
      supportedReadFormats.contains(from) && supportedWriteFormats.contains(to)

  fun convertImage(imageBytes: ByteArray, format: String): ByteArray =
      ByteArrayOutputStream().use {
        val image = ImageIO.read(imageBytes.inputStream())
        ImageIO.write(image, format, it)
        it.toByteArray()
      }
}

fun mediaTypeToImageIOFormat(mediaType: String): String? =
    if (mediaType.startsWith("image/", ignoreCase = true))
      mediaType.toLowerCase().substringAfter("/")
    else
      null