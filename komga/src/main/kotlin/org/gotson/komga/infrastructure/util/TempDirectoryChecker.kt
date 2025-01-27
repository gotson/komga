package org.gotson.komga.infrastructure.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import kotlin.io.path.Path

private val logger = KotlinLogging.logger {}

/**
 * Checks that the temp directory, obtained with System.getProperty("java.io.tmpdir"),
 * exists and is accessible.
 * If the directory does not exist, this will attempt to create it.
 *
 * @throws IllegalStateException if the temp directory cannot be created,
 * or is not accessible, or if the system property 'java.io.tmpdir' is not defined.
 */
fun checkTempDirectory() {
  System.getProperty("java.io.tmpdir")?.let {
    val tmpDir = Path(it)

    if (Files.notExists(tmpDir)) {
      logger.warn { "Temp directory does not exist, attempting to create it: $tmpDir" }
      try {
        Files.createDirectories(tmpDir)
        logger.info { "Created missing temp directory: $tmpDir" }
      } catch (e: Exception) {
        logger.error(e) { "Could not create missing temp directory: $tmpDir" }
        throw IllegalStateException("Could not create missing temp directory: $tmpDir", e)
      }
    }

    check(Files.isWritable(tmpDir)) { "Temp directory is not writable: $tmpDir" }
  } ?: throw IllegalStateException("System property 'java.io.tmpdir' is not defined")
}
