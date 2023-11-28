package org.gotson.komga.infrastructure.image

import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.roundToInt

@Service
class MosaicGenerator(
  private val komgaSettingsProvider: KomgaSettingsProvider,
  @Qualifier("thumbnailType")
  private val thumbnailType: ImageType,
  private val imageConverter: ImageConverter,
) {
  private val ratio = 0.7066666667

  fun createMosaic(images: List<ByteArray>): ByteArray {
    val height = komgaSettingsProvider.thumbnailSize.maxEdge
    val width = (height * ratio).roundToInt()
    val thumbs = images.map { imageConverter.resizeImageToBufferedImage(it, thumbnailType, height / 2) }

    return ByteArrayOutputStream().use { baos ->
      val mosaic = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
      mosaic.createGraphics().apply {
        listOf(
          0 to 0,
          width / 2 to 0,
          0 to height / 2,
          width / 2 to height / 2,
        ).forEachIndexed { index, (x, y) ->
          thumbs.getOrNull(index)?.let { drawImage(it, x, y, null) }
        }
      }

      ImageIO.write(mosaic, thumbnailType.imageIOFormat, baos)

      baos.toByteArray()
    }
  }
}
