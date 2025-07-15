package org.gotson.komga.infrastructure.kobo

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.gotson.komga.domain.model.BookWithMedia
import org.gotson.komga.domain.model.MediaType
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.infrastructure.configuration.SettingChangedEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.nameWithoutExtension

private val logger = KotlinLogging.logger {}

@Component
class KepubConverter(
  private val settingsProvider: KomgaSettingsProvider,
  @param:Value($$"${komga.kobo.kepubify-path:#{null}}") val kepubifyConfigurationPath: String?,
) {
  final var kepubifyPath: Path? = null
    private set
  final var isAvailable = false
    private set

  private val tmpDir by lazy { Path(System.getProperty("java.io.tmpdir")) }

  @PostConstruct
  private fun configureKepubifyOnStartup() {
    if (!settingsProvider.kepubifyPath.isNullOrBlank())
      configureKepubify(settingsProvider.kepubifyPath, true)
    else if (!kepubifyConfigurationPath.isNullOrBlank())
      configureKepubify(kepubifyConfigurationPath)
    else
      logger.info { "Kepub conversion unavailable. kepubify path is not set" }
  }

  @EventListener(SettingChangedEvent.KepubifyPath::class)
  private fun configureKepubifyOnSettingsChange() {
    configureKepubify(settingsProvider.kepubifyPath, true)
  }

  /**
   * Configure the path for kepubify
   * @param newValue path to kepubify
   * @param fallback whether to fallback to configuration properties in case [newValue] is invalid
   */
  fun configureKepubify(
    newValue: String?,
    fallback: Boolean = false,
  ) {
    if (newValue.isNullOrBlank()) {
      isAvailable = false
      kepubifyPath = null
      if (fallback && !kepubifyConfigurationPath.isNullOrBlank()) {
        configureKepubify(kepubifyConfigurationPath)
      }
    } else {
      val newPath = Path(newValue)
      if (!isExecutable(newPath)) {
        logger.warn { "kepubify path is not executable, not found or not valid: $newPath" }
        isAvailable = false
        if (fallback && !kepubifyConfigurationPath.isNullOrBlank()) {
          configureKepubify(kepubifyConfigurationPath)
        }
      } else {
        logger.info { "Kepub conversion available. kepubify path: $newPath" }
        isAvailable = true
      }
      kepubifyPath = newPath
    }
  }

  private fun isExecutable(path: Path): Boolean {
    try {
      if (Files.isExecutable(path)) return true
      // path may be an executable in the PATH, try running it
      val process = Runtime.getRuntime().exec(path.toString())
      process.waitFor(3, TimeUnit.SECONDS)

      return process.exitValue() == 0
    } catch (e: Exception) {
      logger.warn(e) { "Error while verifying executable: $path" }
      return false
    }
  }

  /**
   * Converts an EPUB book to KEPUB. The destination filename will be built from the original book file name.
   *
   * @param bookWithMedia the source book
   * @param destinationDir the destination directory in which to save the converted file, else the default temporary directory is used
   * @throws IllegalArgumentException if the source book is not an EPUB, or is already a KEPUB
   * @return the [Path] of the converted file in case of success, else null
   */
  fun convertEpubToKepub(
    bookWithMedia: BookWithMedia,
    destinationDir: Path? = null,
  ): Path? {
    require(bookWithMedia.media.mediaType == MediaType.EPUB.type) { "Cannot convert, not an EPUB: ${bookWithMedia.book.path}" }
    require(!bookWithMedia.media.epubIsKepub) { "Cannot convert, EPUB is already a KEPUB: ${bookWithMedia.book.path}" }
    require(bookWithMedia.book.path.exists()) { "Source file does not exist: ${bookWithMedia.book.path}" }

    return convertEpubToKepubWithoutChecks(bookWithMedia.book.path, destinationDir)
  }

  /**
   * Converts an EPUB book to KEPUB. The destination filename will be built from the original file name.
   * This function does not check whether the file is an EPUB, or is already a KEPUB, or if the source file exists.
   *
   * This is intended for internal use in the EpubExtractor
   */
  fun convertEpubToKepubWithoutChecks(
    epub: Path,
    destinationDir: Path? = null,
  ): Path? {
    check(isAvailable) { "Kepub conversion is not available, kepubify path may not be set, or may be invalid" }

    if (destinationDir != null) require(destinationDir.isDirectory()) { "Destination directory does not exist: $destinationDir" }

    // kepubify will only convert when the destination name has the .kepub.epub extension, so we have to force it
    val destinationPath = (destinationDir ?: tmpDir).resolve(epub.nameWithoutExtension + ".kepub.epub")
    destinationPath.deleteIfExists()

    val command =
      arrayOf(
        kepubifyPath.toString(),
        epub.toString(),
        "-o",
        destinationPath.toString(),
      )
    logger.debug { "Starting conversion with: ${command.joinToString(" ")}" }
    val process =
      try {
        Runtime.getRuntime().exec(command)
      } catch (e: Exception) {
        logger.error(e) { "Failed to create process" }
        return null
      }

    if (!process.waitFor(10, TimeUnit.SECONDS)) {
      logger.error { "Kepub conversion timeout. Command: ${command.joinToString(" ")}" }
      return null
    }

    if (process.exitValue() != 0) {
      val error = process.errorReader().useLines { it.joinToString(" ") }
      logger.error { "Kepub conversion failed. Command: ${command.joinToString(" ")}. Error: $error" }
      return null
    }
    logger.debug {
      "kepubify output: " + process.inputReader().useLines { it.joinToString("\n") }
    }

    if (!destinationPath.exists()) {
      logger.error { "Converted file not found: $destinationPath" }
      return null
    }

    return destinationPath
  }
}
