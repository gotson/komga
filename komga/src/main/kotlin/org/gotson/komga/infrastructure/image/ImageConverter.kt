package org.gotson.komga.infrastructure.image

import com.luciad.imageio.webp.WebP
import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.spi.IIORegistry
import javax.imageio.spi.ImageReaderSpi

private val logger = KotlinLogging.logger {}

@Service
class ImageConverter {

  val supportedReadFormats by lazy { ImageIO.getReaderFormatNames().toList() }
  val supportedReadMediaTypes by lazy { ImageIO.getReaderMIMETypes().toList() }
  val supportedWriteFormats by lazy { ImageIO.getWriterFormatNames().toList() }
  val supportedWriteMediaTypes by lazy { ImageIO.getWriterMIMETypes().toList() }

  init {
    val registry = IIORegistry.getDefaultInstance()
    val nativeWebp = registry.getServiceProviderByClass(Class.forName("com.luciad.imageio.webp.WebPImageReaderSpi")) as ImageReaderSpi?
    val javaWebp = registry.getServiceProviderByClass(Class.forName("net.sf.javavp8decoder.imageio.WebPImageReaderSpi")) as ImageReaderSpi?

    if (nativeWebp != null) {
      if (!WebP.loadNativeLibrary()) {
        logger.warn { "Could not load native WebP library" }
        registry.deregisterServiceProvider(nativeWebp)
      } else if (javaWebp != null) {
        logger.info { "Using native WebP library" }
        registry.setOrdering(ImageReaderSpi::class.java, nativeWebp, javaWebp)
      }
    }

    logger.info { "Supported read formats: $supportedReadFormats" }
    logger.info { "Supported read mediaTypes: $supportedReadMediaTypes" }
    logger.info { "Supported write formats: $supportedWriteFormats" }
    logger.info { "Supported write mediaTypes: $supportedWriteMediaTypes" }
  }

  private val supportsTransparency = listOf("png")

  fun convertImage(imageBytes: ByteArray, format: String): ByteArray =
    ByteArrayOutputStream().use { baos ->
      val image = ImageIO.read(imageBytes.inputStream())

      val result = if (!supportsTransparency.contains(format) && containsAlphaChannel(image)) {
        if (containsTransparency(image)) logger.info { "Image contains alpha channel but is not opaque, visual artifacts may appear" }
        else logger.info { "Image contains alpha channel but is opaque, conversion should not generate any visual artifacts" }
        BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB).also {
          it.createGraphics().drawImage(image, 0, 0, Color.WHITE, null)
        }
      } else {
        image
      }

      ImageIO.write(result, format, baos)

      baos.toByteArray()
    }

  fun resizeImage(imageBytes: ByteArray, format: String, size: Int): ByteArray =
    ByteArrayOutputStream().use {
      Thumbnails.of(imageBytes.inputStream())
        .size(size, size)
        .outputFormat(format)
        .toOutputStream(it)
      it.toByteArray()
    }

  private fun containsAlphaChannel(image: BufferedImage): Boolean =
    image.colorModel.hasAlpha()

  private fun containsTransparency(image: BufferedImage): Boolean {
    for (x in 0 until image.width) {
      for (y in 0 until image.height) {
        val pixel = image.getRGB(x, y)
        if (pixel shr 24 == 0x00) return true
      }
    }
    return false
  }
}
