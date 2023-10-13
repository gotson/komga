package org.gotson.komga.infrastructure.datasource

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywaySecondaryMigrationInitializer(
  @Qualifier("tasksDataSource")
  private val tasksDataSource: DataSource,
) : InitializingBean {

  // by default Spring Boot will perform migration only on the @Primary datasource
  override fun afterPropertiesSet() {
    Flyway.configure()
      .locations("classpath:tasks/migration/sqlite")
      .dataSource(tasksDataSource)
      .load()
      .apply {
        migrate()
      }
  }
}
