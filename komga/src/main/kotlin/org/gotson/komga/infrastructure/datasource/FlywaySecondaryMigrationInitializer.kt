package org.gotson.komga.infrastructure.datasource

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class FlywaySecondaryMigrationInitializer(
  @Qualifier("tasksDataSourceRW")
  private val tasksDataSource: DataSource,
) : InitializingBean {
  // by default Spring Boot will perform migration only on the @Primary datasource
  override fun afterPropertiesSet() {
    Flyway
      .configure()
      .locations("classpath:tasks/migration/sqlite")
      .dataSource(tasksDataSource)
      .load()
      .apply {
        migrate()
      }
  }
}
