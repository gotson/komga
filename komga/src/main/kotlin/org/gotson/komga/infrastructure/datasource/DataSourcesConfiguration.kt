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

    val sqliteUdfDataSource = DataSourceBuilder.create()
      .driverClassName("org.sqlite.JDBC")
      .url("jdbc:sqlite:${komgaProperties.database.file}?foreign_keys=on;")
      .type(SqliteUdfDataSource::class.java)
      .build()

    val poolSize =
      if (komgaProperties.database.file.contains(":memory:")) 1
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
