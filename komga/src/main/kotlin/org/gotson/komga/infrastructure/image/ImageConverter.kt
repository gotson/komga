package org.gotson.komga.infrastructure.image

import io.github.oshai.kotlinlogging.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.spi.IIORegistry
import javax.imageio.spi.ImageReaderSpi
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

private const val WEBP_NIGHT_MONKEYS = "com.github.gotson.nightmonkeys.webp.imageio.plugins.WebpImageReaderSpi"

@Service
class ImageConverter(
  private val imageAnalyzer: ImageAnalyzer,
  private val contentDetector: ContentDetector,
) {
  val supportedReadFormats by lazy { ImageIO.getReaderFormatNames().toList() }
  val supportedReadMediaTypes by lazy { ImageIO.getReaderMIMETypes().toList() }
  val supportedWriteFormats by lazy { ImageIO.getWriterFormatNames().toList() }
  val supportedWriteMediaTypes by lazy { ImageIO.getWriterMIMETypes().toList() }

  init {
    chooseWebpReader()
    logger.info { "Supported read formats: $supportedReadFormats" }
    logger.info { "Supported read mediaTypes: $supportedReadMediaTypes" }
    logger.info { "Supported write formats: $supportedWriteFormats" }
    logger.info { "Supported write mediaTypes: $supportedWriteMediaTypes" }
  }

  private fun chooseWebpReader() {
    val providers =
      IIORegistry
        .getDefaultInstance()
        .getServiceProviders(
          ImageReaderSpi::class.java,
          { it is ImageReaderSpi && it.mimeTypes.contains("image/webp") },
          false,
        ).asSequence()
        .toList()

    if (providers.size > 1) {
      logger.debug { "WebP reader providers: ${providers.map { it.javaClass.canonicalName }}" }
      providers.firstOrNull { it.javaClass.canonicalName == WEBP_NIGHT_MONKEYS }?.let { nightMonkeys ->
        (providers - nightMonkeys).forEach {
          logger.debug { "Deregister provider: ${it.javaClass.canonicalName}" }
          IIORegistry.getDefaultInstance().deregisterServiceProvider(it)
        }
      }
    }
  }

  private val supportsTransparency = listOf("png")

  fun canConvertMediaType(
    from: String,
    to: String,
  ) = supportedReadMediaTypes.contains(from) && supportedWriteMediaTypes.contains(to)

  fun convertImage(
    imageBytes: ByteArray,
    format: String,
  ): ByteArray =
    ByteArrayOutputStream().use { baos ->
      val image = ImageIO.read(imageBytes.inputStream())

      val result =
        if (!supportsTransparency.contains(format) && containsAlphaChannel(image)) {
          if (containsTransparency(image))
            logger.info { "Image contains alpha channel but is not opaque, visual artifacts may appear" }
          else
            logger.info { "Image contains alpha channel but is opaque, conversion should not generate any visual artifacts" }
          BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB).also {
            it.createGraphics().drawImage(image, 0, 0, Color.WHITE, null)
          }
        } else {
          image
        }

      ImageIO.write(result, format, baos)

      baos.toByteArray()
    }

  fun resizeImageToByteArray(
    imageBytes: ByteArray,
    format: ImageType,
    size: Int,
  ): ByteArray {
    val builder = resizeImageBuilder(imageBytes, format, size) ?: return imageBytes

    return ByteArrayOutputStream().use {
      builder.toOutputStream(it)
      it.toByteArray()
    }
  }

  fun resizeImageToBufferedImage(
    imageBytes: ByteArray,
    format: ImageType,
    size: Int,
  ): BufferedImage {
    val builder = resizeImageBuilder(imageBytes, format, size) ?: return ImageIO.read(imageBytes.inputStream())

    return builder.asBufferedImage()
  }

  private fun resizeImageBuilder(
    imageBytes: ByteArray,
    format: ImageType,
    size: Int,
  ): Thumbnails.Builder<out InputStream>? {
    val longestEdge =
      imageAnalyzer.getDimension(imageBytes.inputStream())?.let {
        val mediaType = contentDetector.detectMediaType(imageBytes.inputStream())
        val longestEdge = max(it.height, it.width)
        // don't resize if source and target format is the same, and source is smaller than desired
        if (mediaType == format.mediaType && longestEdge <= size) return null
        longestEdge
      }

    // prevent upscaling
    val resizeTo = if (longestEdge != null) min(longestEdge, size) else size

    return Thumbnails
      .of(imageBytes.inputStream())
      .size(resizeTo, resizeTo)
      .imageType(BufferedImage.TYPE_INT_ARGB)
      .outputFormat(format.imageIOFormat)
  }

  private fun containsAlphaChannel(image: BufferedImage): Boolean = image.colorModel.hasAlpha()

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
