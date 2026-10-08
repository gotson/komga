package org.gotson.komga.infrastructure.session

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.session.SessionProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.jdbc.JdbcIndexedSessionRepository
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@EnableJdbcHttpSession
@Configuration
@ConditionalOnProperty(
  value = ["komga.jdbc-session-enabled"],
  havingValue = "true",
  matchIfMissing = false,
)
class JdbcSessionConfiguration {
  //language=sqlite
  private val sqliteCreateSessionQuery =
    """
    INSERT INTO %TABLE_NAME% (PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME, MAX_INACTIVE_INTERVAL, EXPIRY_TIME, PRINCIPAL_NAME)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(PRIMARY_ID) DO UPDATE SET
        SESSION_ID = excluded.SESSION_ID,
        LAST_ACCESS_TIME = excluded.LAST_ACCESS_TIME,
        MAX_INACTIVE_INTERVAL = excluded.MAX_INACTIVE_INTERVAL,
        EXPIRY_TIME = excluded.EXPIRY_TIME,
        PRINCIPAL_NAME = excluded.PRINCIPAL_NAME
    """.trimIndent()

  //language=sqlite
  private val sqliteCreateSessionAttributeQuery =
    """
    INSERT INTO %TABLE_NAME%_ATTRIBUTES (SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES)
    VALUES (?, ?, ?)
    ON CONFLICT(SESSION_PRIMARY_ID, ATTRIBUTE_NAME) DO UPDATE SET
        ATTRIBUTE_BYTES = excluded.ATTRIBUTE_BYTES
    """.trimIndent()

  @Bean
  fun customizeSessionRepository(sessionProperties: SessionProperties, serverProperties: ServerProperties) =
    SessionRepositoryCustomizer<JdbcIndexedSessionRepository> { repository ->
      repository.setCreateSessionQuery(sqliteCreateSessionQuery)
      repository.setCreateSessionAttributeQuery(sqliteCreateSessionAttributeQuery)

      val timeout = sessionProperties.timeout
        ?: serverProperties.servlet.session.timeout
        ?: 30.minutes.toJavaDuration()
      repository.setDefaultMaxInactiveInterval(timeout)
    }
}
