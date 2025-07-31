package org.gotson.komga.infrastructure.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

@Configuration
class DataSourcesConfiguration(
  private val komgaProperties: KomgaProperties,
) {
  @Bean("sqliteDataSourceRW")
  @Primary
  fun sqliteDataSourceRW(): DataSource =
    buildDataSource("SqliteMainPoolRW", SqliteUdfDataSource::class.java, komgaProperties.database)
      .apply {
        // force pool size to 1 if the pool is only used for writes
        if (komgaProperties.database.shouldSeparateReadFromWrites()) this.maximumPoolSize = 1
      }

  @Bean("sqliteDataSourceRO")
  fun sqliteDataSourceRO(): DataSource =
    if (komgaProperties.database.shouldSeparateReadFromWrites())
      buildDataSource("SqliteMainPoolRO", SqliteUdfDataSource::class.java, komgaProperties.database)
    else
      sqliteDataSourceRW()

  @Bean("tasksDataSourceRW")
  fun tasksDataSourceRW(): DataSource =
    buildDataSource("SqliteTasksPoolRW", SQLiteDataSource::class.java, komgaProperties.tasksDb)
      .apply {
        // pool size is always 1:
        // - if there's only 1 pool for read and writes, size should be 1
        // - if there's a separate read pool, the write pool size should be 1
        this.maximumPoolSize = 1
      }

  @Bean("tasksDataSourceRO")
  fun tasksDataSourceRO(): DataSource =
    if (komgaProperties.tasksDb.shouldSeparateReadFromWrites())
      buildDataSource("SqliteTasksPoolRO", SQLiteDataSource::class.java, komgaProperties.tasksDb)
    else
      tasksDataSourceRW()

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
      if (databaseProps.isMemory())
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

  fun KomgaProperties.Database.isMemory() = file.contains(":memory:") || file.contains("mode=memory")

  fun KomgaProperties.Database.shouldSeparateReadFromWrites(): Boolean = !isMemory() && journalMode == SQLiteConfig.JournalMode.WAL
}
