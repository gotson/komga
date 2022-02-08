package org.gotson.komga.infrastructure.datasource

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// TODO: remove after March 2022 - added to fix the 0.149.0 changed migration
// We should not rely on flyway repair on a permanent basis!
@Configuration
class FlywayConfiguration {

  @Bean
  fun cleanMigrateStrategy() =
    FlywayMigrationStrategy { flyway: Flyway ->
      flyway.repair()
      flyway.migrate()
    }
}
