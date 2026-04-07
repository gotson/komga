package org.gotson.komga.infrastructure.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.postgresql.ds.PGSimpleDataSource
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
  @Bean("mainDataSourceRW")
  @Primary
  fun mainDataSourceRW(): DataSource =
    buildDataSource("MainPoolRW", komgaProperties.database)
      .apply {
        // force pool size to 1 if the pool is only used for writes
        if (komgaProperties.database.shouldSeparateReadFromWrites()) this.maximumPoolSize = 1
      }

  @Bean("mainDataSourceRO")
  fun mainDataSourceRO(): DataSource =
    if (komgaProperties.database.shouldSeparateReadFromWrites())
      buildDataSource("MainPoolRO", komgaProperties.database)
    else
      mainDataSourceRW()

  @Bean("tasksDataSourceRW")
  fun tasksDataSourceRW(): DataSource =
    buildDataSource("TasksPoolRW", komgaProperties.tasksDb)
      .apply {
        // pool size is always 1:
        // - if there's only 1 pool for read and writes, size should be 1
        // - if there's a separate read pool, the write pool size should be 1
        this.maximumPoolSize = 1
      }

  @Bean("tasksDataSourceRO")
  fun tasksDataSourceRO(): DataSource =
    if (komgaProperties.tasksDb.shouldSeparateReadFromWrites())
      buildDataSource("TasksPoolRO", komgaProperties.tasksDb)
    else
      tasksDataSourceRW()

  private fun buildDataSource(
    poolName: String,
    databaseProps: KomgaProperties.Database,
  ): HikariDataSource {
    return when (databaseProps.type) {
      DatabaseType.SQLITE -> buildSqliteDataSource(poolName, databaseProps)
      DatabaseType.POSTGRESQL -> buildPostgresDataSource(poolName, databaseProps)
    }
  }

  private fun buildSqliteDataSource(
    poolName: String,
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
        .type(SqliteUdfDataSource::class.java)
        .build()

    with(dataSource as SqliteUdfDataSource) {
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

  private fun buildPostgresDataSource(
    poolName: String,
    databaseProps: KomgaProperties.Database,
  ): HikariDataSource {
    val dataSource = PGSimpleDataSource().apply {
      databaseProps.url?.let { setURL(it) }
      databaseProps.username?.let { user = it }
      databaseProps.password?.let { password = it }
    }

    val poolSize =
      if (databaseProps.poolSize != null)
        databaseProps.poolSize!!
      else
        Runtime.getRuntime().availableProcessors().coerceAtMost(databaseProps.maxPoolSize)

    return HikariDataSource(
      HikariConfig().apply {
        this.dataSource = dataSource
        this.poolName = poolName
        this.maximumPoolSize = poolSize
        this.minimumIdle = 1
        this.connectionTimeout = 30000
        this.idleTimeout = 600000
        this.maxLifetime = 1800000
      },
    )
  }

  fun KomgaProperties.Database.isMemory() = file.contains(":memory:") || file.contains("mode=memory")

  fun KomgaProperties.Database.shouldSeparateReadFromWrites(): Boolean = 
    when (type) {
      DatabaseType.SQLITE -> !isMemory() && journalMode == SQLiteConfig.JournalMode.WAL
      DatabaseType.POSTGRESQL -> false // PostgreSQL doesn't need separate read/write pools
    }
}
