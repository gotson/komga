package org.gotson.komga.infrastructure.datasource

import com.zaxxer.hikari.HikariDataSource
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.relational.core.dialect.Dialect
import org.springframework.data.relational.core.dialect.H2Dialect
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import javax.sql.DataSource

@Configuration
class DataSourcesConfiguration(
  private val komgaProperties: KomgaProperties
) : AbstractJdbcConfiguration() {

  @Bean("sqliteDataSource")
  @Primary
  fun sqliteDataSource(): DataSource =
    (
      DataSourceBuilder.create()
        .apply {
          driverClassName("org.sqlite.JDBC")
          url("jdbc:sqlite:${komgaProperties.database.file}?foreign_keys=on;")
        }.type(HikariDataSource::class.java)
        .build() as HikariDataSource
      )
      .apply { maximumPoolSize = 1 }

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  fun h2DataSourceProperties() = DataSourceProperties()

  @Bean("h2DataSource")
  fun h2DataSource(): DataSource =
    h2DataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource::class.java).build()

  @Bean
  override fun jdbcDialect(operations: NamedParameterJdbcOperations): Dialect = H2Dialect.INSTANCE
}
