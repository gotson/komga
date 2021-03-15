package org.gotson.komga.infrastructure.datasource

import mu.KotlinLogging
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.PostConstruct
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries

private val logger = KotlinLogging.logger {}

@Component
@Profile("!test")
class DatabaseMigration(
  private val komgaProperties: KomgaProperties
) {

  @ExperimentalPathApi
  @PostConstruct
  fun init() {
    convertHomeDir(komgaProperties.database.file).parent.listDirectoryEntries("*.mv.db*").let { h2Files ->
      if (h2Files.isNotEmpty()) {
        logger.info { "Deleting old H2 database files" }
        h2Files.forEach {
          logger.info { "Delete: $it" }
          it.deleteIfExists()
        }
      }
    }
  }
}

fun convertHomeDir(path: String): Path {
  val aPath = Paths.get(path)
  val components = aPath.toList()

  return if (components.first().toString() == "~") {
    Paths.get(System.getProperty("user.home"), *components.drop(1).map { it.toString() }.toTypedArray())
  } else aPath
}
