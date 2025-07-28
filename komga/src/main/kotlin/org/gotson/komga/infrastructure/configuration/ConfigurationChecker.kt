package org.gotson.komga.infrastructure.configuration

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.gotson.komga.domain.model.ConfigurationException
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

private val logger = KotlinLogging.logger {}

@Component
class ConfigurationChecker(
  private val komgaProperties: KomgaProperties,
) {
  private val blockTypes = listOf("cifs", "nfs")

  @PostConstruct
  fun checkDatabasesPath() {
    checkDatabaseIsLocal(komgaProperties.database, "komga.database.check-local-filesystem: false")
    checkDatabaseIsLocal(komgaProperties.tasksDb, "komga.tasks-db.check-local-filesystem: false")
  }

  private fun checkDatabaseIsLocal(
    database: KomgaProperties.Database,
    ignoreProp: String,
  ) {
    if (database.checkLocalFilesystem) {
      val path =
        try {
          Path(database.file)
        } catch (_: Exception) {
          return
        }
      checkIfRemote(path, ignoreProp)
      if (!path.exists()) checkIfRemote(path.parent, ignoreProp)
    }
  }

  private fun checkIfRemote(
    path: Path?,
    ignoreProp: String,
  ) {
    if (path == null) return
    if (path.exists()) {
      val storeType =
        try {
          Files.getFileStore(path).type().lowercase()
        } catch (e: Exception) {
          logger.warn(e) { "Could not get FileStore type for path: $path" }
          "unknown"
        }

      if (blockTypes.any { storeType.startsWith(it, ignoreCase = true) }) {
        val errorMessage = "The path '$path' should be on a local filesystem, but was detected to be on a remote filesystem ($storeType). If this is inaccurate you can set '$ignoreProp' to ignore this check."

        logger.error { errorMessage }
        throw ConfigurationException(errorMessage)
      } else {
        logger.debug { "FileStore type: $storeType, path: $path" }
      }
    }
  }
}
