package org.gotson.komga.infrastructure.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourcesConfiguration(
  private val komgaProperties: KomgaProperties,
) {

  @Bean("sqliteDataSource")
  @Primary
  fun sqliteDataSource(): DataSource {

    val extraPragmas = komgaProperties.database.pragmas.let {
      if (it.isEmpty()) ""
      else "?" + it.map { (key, value) -> "$key=$value" }.joinToString(separator = "&")
    }

    val sqliteUdfDataSource = DataSourceBuilder.create()
      .driverClassName("org.sqlite.JDBC")
      .url("jdbc:sqlite:${komgaProperties.database.file}$extraPragmas")
      .type(SqliteUdfDataSource::class.java)
      .build()

    sqliteUdfDataSource.setEnforceForeignKeys(true)
    with(komgaProperties.database) {
      journalMode?.let { sqliteUdfDataSource.setJournalMode(it.name) }
      busyTimeout?.let { sqliteUdfDataSource.config.busyTimeout = it.toMillis().toInt() }
    }

    val poolSize =
      if (komgaProperties.database.file.contains(":memory:")) 1
      else if (komgaProperties.database.poolSize != null) komgaProperties.database.poolSize!!
      else Runtime.getRuntime().availableProcessors().coerceAtMost(komgaProperties.database.maxPoolSize)

    return HikariDataSource(
      HikariConfig().apply {
        dataSource = sqliteUdfDataSource
        poolName = "SqliteUdfPool"
        maximumPoolSize = poolSize
      },
    )
  }
}
