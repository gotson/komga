package org.gotson.komga.domain.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

/**
 * Service for splitting tall images (webtoons) into multiple pages.
 */
@Service
class ImageSplitterService {
  /**
   * Split a tall image into multiple smaller images.
   * @param imageBytes The original image bytes
   * @param heightThreshold Images taller than this will be split (default 3000px)
   * @param splitHeight Height of each split image (default 1500px)
   * @return List of image bytes, or single-item list if no splitting needed
   */
  fun splitTallImage(
    imageBytes: ByteArray,
    heightThreshold: Int = 3000,
    splitHeight: Int = 1500,
  ): List<ByteArray> {
    try {
      val originalImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        ?: return listOf(imageBytes)

      val width = originalImage.width
      val height = originalImage.height

      // Don't split if below threshold
      if (height <= heightThreshold) {
        return listOf(imageBytes)
      }

      logger.info { "Splitting tall image: ${width}x${height} into segments of height $splitHeight" }

      val splitImages = mutableListOf<ByteArray>()
      var currentY = 0

      while (currentY < height) {
        val remainingHeight = height - currentY
        val currentSplitHeight = minOf(splitHeight, remainingHeight)

        // Extract sub-image
        val subImage = originalImage.getSubimage(
          0,
          currentY,
          width,
          currentSplitHeight,
        )

        // Convert to bytes
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(subImage, getImageFormat(imageBytes), outputStream)
        splitImages.add(outputStream.toByteArray())

        currentY += currentSplitHeight
      }

      logger.info { "Split image into ${splitImages.size} parts" }
      return splitImages
    } catch (e: Exception) {
      logger.error(e) { "Failed to split image, returning original" }
      return listOf(imageBytes)
    }
  }

  /**
   * Check if an image should be split based on its dimensions.
   * @param imageBytes The image bytes
   * @param heightThreshold Height threshold for splitting
   * @return true if image should be split
   */
  fun shouldSplitImage(imageBytes: ByteArray, heightThreshold: Int = 3000): Boolean {
    try {
      val image = ImageIO.read(ByteArrayInputStream(imageBytes))
      return image != null && image.height > heightThreshold
    } catch (e: Exception) {
      return false
    }
  }

  /**
   * Get the image format from the byte array.
   * @param imageBytes The image bytes
   * @return Image format (jpg, png, etc.)
   */
  private fun getImageFormat(imageBytes: ByteArray): String {
    return when {
      imageBytes.size >= 2 && imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte() -> "jpg"
      imageBytes.size >= 8 &&
        imageBytes[0] == 0x89.toByte() &&
        imageBytes[1] == 0x50.toByte() &&
        imageBytes[2] == 0x4E.toByte() &&
        imageBytes[3] == 0x47.toByte() -> "png"
      imageBytes.size >= 4 &&
        imageBytes[0] == 0x47.toByte() &&
        imageBytes[1] == 0x49.toByte() &&
        imageBytes[2] == 0x46.toByte() -> "gif"
      imageBytes.size >= 12 &&
        imageBytes[8] == 0x57.toByte() &&
        imageBytes[9] == 0x45.toByte() &&
        imageBytes[10] == 0x42.toByte() &&
        imageBytes[11] == 0x50.toByte() -> "webp"
      else -> "jpg" // Default to jpg
    }
  }
}
