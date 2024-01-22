package org.gotson.komga.infrastructure.jms

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import java.nio.file.Path
import kotlin.io.path.isDirectory

private val logger = KotlinLogging.logger {}

@Service
@Deprecated("To be removed")
class ArtemisCleanup(
  @Value("\${spring.artemis.embedded.data-directory}") private val artemisDataDir: String? = null,
) : InitializingBean {
  override fun afterPropertiesSet() {
    try {
      artemisDataDir?.let {
        val artemisDataPath = Path.of(it)
        if (artemisDataPath.isDirectory()) {
          logger.info { "Deleting the legacy Artemis data-directory: $artemisDataPath" }
          FileSystemUtils.deleteRecursively(artemisDataPath)
        }
      }
    } catch (e: Exception) {
      logger.warn(e) { "Failed to cleanup the Artemis directory" }
    }
  }
}
