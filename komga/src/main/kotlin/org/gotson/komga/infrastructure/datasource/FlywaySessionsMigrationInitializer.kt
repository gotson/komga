package org.gotson.komga.infrastructure.datasource

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import javax.sql.DataSource

@ConditionalOnProperty(
  value = ["komga.jdbc-session-enabled"],
  havingValue = "true",
  matchIfMissing = false,
)
@Component
class FlywaySessionsMigrationInitializer(
  @Qualifier("sessionsDataSource")
  private val sessionsDataSource: DataSource,
) : InitializingBean {
  // by default Spring Boot will perform migration only on the @Primary datasource
  override fun afterPropertiesSet() {
    Flyway
      .configure()
      .locations("classpath:sessions/migration/sqlite")
      .dataSource(sessionsDataSource)
      .load()
      .apply {
        migrate()
      }
  }
}
