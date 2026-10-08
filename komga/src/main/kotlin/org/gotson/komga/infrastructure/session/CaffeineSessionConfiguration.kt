package org.gotson.komga.infrastructure.session

import com.github.gotson.spring.session.caffeine.CaffeineIndexedSessionRepository
import com.github.gotson.spring.session.caffeine.config.annotation.web.http.EnableCaffeineHttpSession
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.session.SessionProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.config.SessionRepositoryCustomizer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@EnableCaffeineHttpSession
@Configuration
@ConditionalOnProperty(
  value = ["komga.jdbc-session-enabled"],
  havingValue = "false",
  matchIfMissing = true,
)
class CaffeineSessionConfiguration {
  @Bean
  fun customizeSessionRepository(sessionProperties: SessionProperties,serverProperties: ServerProperties) =
    SessionRepositoryCustomizer<CaffeineIndexedSessionRepository> { repository ->
      val timeout = sessionProperties.timeout
        ?: serverProperties.servlet.session.timeout
        ?: 30.minutes.toJavaDuration()
      repository.setDefaultMaxInactiveInterval(timeout.seconds.toInt())
    }
}
