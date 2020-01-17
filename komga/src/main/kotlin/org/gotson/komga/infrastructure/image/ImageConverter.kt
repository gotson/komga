package org.gotson.komga.infrastructure.image

import mu.KotlinLogging
import org.imgscalr.Scalr
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

@Service
class ImageConverter {

  val supportedReadFormats = ImageIO.getReaderFormatNames().toList()
  val supportedReadMediaTypes = ImageIO.getReaderMIMETypes().toList()
  val supportedWriteFormats = ImageIO.getWriterFormatNames().toList()
  val supportedWriteMediaTypes = ImageIO.getWriterMIMETypes().toList()

  init {
    logger.info { "Supported read formats: $supportedReadFormats" }
    logger.info { "Supported read mediaTypes: $supportedReadMediaTypes" }
    logger.info { "Supported write formats: $supportedWriteFormats" }
    logger.info { "Supported write mediaTypes: $supportedWriteMediaTypes" }
  }

  fun convertImage(imageBytes: ByteArray, format: String): ByteArray =
    ByteArrayOutputStream().use {
      val image = ImageIO.read(imageBytes.inputStream())
      ImageIO.write(image, format, it)
      it.toByteArray()
    }

  fun resizeImage(imageBytes: ByteArray, format: String, size: Int): ByteArray =
    ByteArrayOutputStream().use {
      ImageIO.write(Scalr.resize(ImageIO.read(imageBytes.inputStream()), Scalr.Method.AUTOMATIC, size), format, it)
      it.toByteArray()
    }
}
