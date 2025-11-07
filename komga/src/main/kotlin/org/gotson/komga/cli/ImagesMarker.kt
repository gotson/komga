package org.gotson.komga.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.tika.config.TikaConfig
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.infrastructure.mediacontainer.ContentDetector
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

object ImagesMarker {
  /**
   * min value of images in a folder that to mark as a komga_images
   */
  private const val images_min = 10
  private val contentDetector = ContentDetector(TikaConfig.getDefaultConfig())

  /**
   * images_mark dir1 dir2 ...
   */
  fun handler(args: Array<String>) {
    if (args.size < 2) {
      logger.error { "Usage: images_mark dir1 dir2 ..." }
      return
    }

    // Start from index 1, as args[0] is "images_mark"
    val directories = args.drop(1)

    for (dirPath in directories) {
      try {
        processDirectory(Paths.get(dirPath))
      } catch (e: Exception) {
        logger.error(e) { "Failed to process directory: $dirPath" }
      }
    }
  }

  private fun processDirectory(dir: Path) {
    if (!Files.exists(dir) || !Files.isDirectory(dir)) {
      logger.warn { "Directory does not exist or is not a directory: $dir" }
      return
    }

    val markerExtension = MediaType.IMAGES.fileExtension

    // Check for marker file and count images in a single pass, also collect subdirectories
    var hasMarkerFile = false
    var imageCount = 0L
    val subdirectories = mutableListOf<Path>()

    Files.list(dir).use { dirStream ->
      for (file in dirStream) {
        // Check if it's a marker file
        if (file.name.endsWith(".$markerExtension", ignoreCase = true)) {
          hasMarkerFile = true
          break
        }
        // Check if it's a subdirectory
        if (Files.isDirectory(file)) {
          subdirectories.add(file)
        } else {
          // Count images
          if (contentDetector.isImage(contentDetector.detectMediaType(file))) {
            imageCount++
          }
        }
      }
    }

    if (hasMarkerFile) {
      logger.info { "Directory $dir already has a marker file, skipping" }
      return
    }

    logger.info { "Directory $dir contains $imageCount image files" }

    if (imageCount >= images_min) {
      val dirName = dir.name
      val markerFileName = "$dirName.$markerExtension"
      val markerFile = dir.resolve(markerFileName)
      try {
        Files.createFile(markerFile)
        logger.info { "Created marker file: $markerFile" }
      } catch (e: Exception) {
        logger.error(e) { "Failed to create marker file: $markerFile" }
      }
    } else {
      logger.info { "Directory $dir has $imageCount images (not exceeding $images_min), marker file not created" }
    }

    // Recursively process subdirectories
    for (subdir in subdirectories) {
      try {
        processDirectory(subdir)
      } catch (e: Exception) {
        logger.error(e) { "Failed to process subdirectory: $subdir" }
      }
    }
  }
}
