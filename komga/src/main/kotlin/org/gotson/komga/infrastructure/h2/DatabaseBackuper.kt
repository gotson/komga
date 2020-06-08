package org.gotson.komga.infrastructure.h2

import mu.KotlinLogging
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

@Component
class DatabaseBackuper(
  private val jdbcTemplate: JdbcTemplate,
  private val komgaProperties: KomgaProperties
) {

  fun backupDatabase() {
    val path = Paths.get(komgaProperties.databaseBackup.path)

    Files.deleteIfExists(path)

    val command = "BACKUP TO '$path'"

    logger.info { "Executing command: $command" }
    jdbcTemplate.execute(command)
  }
}
