package org.gotson.komga.infrastructure.image

import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class MosaicGenerator {

  fun createMosaic(images: List<ByteArray>): ByteArray {
    val thumbs = images.map { resize(it, 150) }

    return ByteArrayOutputStream().use { baos ->
      val mosaic = BufferedImage(212, 300, BufferedImage.TYPE_INT_RGB)
      mosaic.createGraphics().apply {
        listOf(
          0 to 0,
          106 to 0,
          0 to 150,
          106 to 150,
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
