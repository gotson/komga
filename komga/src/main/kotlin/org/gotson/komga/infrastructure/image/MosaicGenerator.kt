package org.gotson.komga.infrastructure.image

import net.coobird.thumbnailator.Thumbnails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class MosaicGenerator(
  @Value("#{@komgaProperties.thumbnailHeight}") private val thumbnailHeight: Int,
) {

  fun createMosaic(images: List<ByteArray>): ByteArray {
    val thumbs = images.map { resize(it, thumbnailHeight / 2) }

    val thumbnailWidth: Int = 212 * thumbnailHeight / 300

    return ByteArrayOutputStream().use { baos ->
      val mosaic = BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB)
      mosaic.createGraphics().apply {
        listOf(
          0 to 0,
          (thumbnailWidth / 2) to 0,
          0 to (thumbnailHeight / 2),
          (thumbnailWidth / 2) to (thumbnailHeight / 2),
        ).forEachIndexed { index, (x, y) ->
          thumbs.getOrNull(index)?.let { drawImage(it, x, y, null) }
        }
      }

      ImageIO.write(mosaic, "jpeg", baos)

      baos.toByteArray()
    }
  }

  private fun resize(imageBytes: ByteArray, size: Int) =
    Thumbnails.of(imageBytes.inputStream())
      .size(size, size)
      .outputFormat("jpeg")
      .asBufferedImage()
}
