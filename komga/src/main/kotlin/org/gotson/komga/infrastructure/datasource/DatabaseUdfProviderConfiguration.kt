package org.gotson.komga.infrastructure.datasource

import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseUdfProviderConfiguration(
  private val komgaProperties: KomgaProperties,
) {
  @Bean
  fun databaseUdfProvider(): DatabaseUdfProvider =
    when (komgaProperties.database.type) {
      DatabaseType.SQLITE -> SqliteUdfProvider()
      DatabaseType.POSTGRESQL -> PostgresUdfProvider()
    }

  @Bean
  fun tasksDatabaseUdfProvider(): DatabaseUdfProvider =
    when (komgaProperties.tasksDb.type) {
      DatabaseType.SQLITE -> SqliteUdfProvider()
      DatabaseType.POSTGRESQL -> PostgresUdfProvider()
    }
}
