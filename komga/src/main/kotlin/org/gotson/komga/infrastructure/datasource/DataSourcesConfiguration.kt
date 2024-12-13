package org.gotson.komga.infrastructure.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

@Configuration
class DataSourcesConfiguration(
  private val komgaProperties: KomgaProperties,
) {
  @Bean("sqliteDataSource")
  @Primary
  fun sqliteDataSource(): DataSource = buildDataSource("SqliteMainPool", SqliteUdfDataSource::class.java, komgaProperties.database)

  @Bean("tasksDataSource")
  fun tasksDataSource(): DataSource =
    buildDataSource("SqliteTasksPool", SQLiteDataSource::class.java, komgaProperties.tasksDb)
      .apply {
        // force pool size to 1 for tasks datasource
        this.maximumPoolSize = 1
      }

  private fun buildDataSource(
    poolName: String,
    dataSourceClass: Class<out SQLiteDataSource>,
    databaseProps: KomgaProperties.Database,
  ): HikariDataSource {
    val extraPragmas =
      databaseProps.pragmas.let {
        if (it.isEmpty())
          ""
        else
          "?" + it.map { (key, value) -> "$key=$value" }.joinToString(separator = "&")
      }

    val dataSource =
      DataSourceBuilder
        .create()
        .driverClassName("org.sqlite.JDBC")
        .url("jdbc:sqlite:${databaseProps.file}$extraPragmas")
        .type(dataSourceClass)
        .build()

    with(dataSource) {
      setEnforceForeignKeys(true)
      setGetGeneratedKeys(false)
    }
    with(databaseProps) {
      journalMode?.let { dataSource.setJournalMode(it.name) }
      busyTimeout?.let { dataSource.config.busyTimeout = it.toMillis().toInt() }
    }

    val poolSize =
      if (databaseProps.file.contains(":memory:") || databaseProps.file.contains("mode=memory"))
        1
      else if (databaseProps.poolSize != null)
        databaseProps.poolSize!!
      else
        Runtime.getRuntime().availableProcessors().coerceAtMost(databaseProps.maxPoolSize)

    return HikariDataSource(
      HikariConfig().apply {
        this.dataSource = dataSource
        this.poolName = poolName
        this.maximumPoolSize = poolSize
      },
    )
  }
}
