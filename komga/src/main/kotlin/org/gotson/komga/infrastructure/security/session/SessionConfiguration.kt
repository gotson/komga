package org.gotson.komga.infrastructure.security.session

import com.github.gotson.spring.session.caffeine.CaffeineIndexedSessionRepository
import com.github.gotson.spring.session.caffeine.config.annotation.web.http.EnableCaffeineHttpSession
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.session.SessionRegistry
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
import org.springframework.session.web.http.HttpSessionIdResolver

@EnableCaffeineHttpSession
@Configuration
class SessionConfiguration {
  @Bean
  fun sessionCookieName() = "SESSION"

  @Bean
  fun sessionHeaderName() = "X-Auth-Token"

  @Bean
  fun cookieSerializer(sessionCookieName: String): CookieSerializer =
    DefaultCookieSerializer().apply {
      setCookieName(sessionCookieName)
    }

  @Bean
  fun httpSessionIdResolver(
    sessionHeaderName: String,
    cookieSerializer: CookieSerializer,
  ): HttpSessionIdResolver = SmartHttpSessionIdResolver(sessionHeaderName, cookieSerializer)

  @Bean
  fun customizeSessionRepository(serverProperties: ServerProperties) =
    SessionRepositoryCustomizer<CaffeineIndexedSessionRepository> {
      it.setDefaultMaxInactiveInterval(
        serverProperties.servlet.session.timeout.seconds
          .toInt(),
      )
    }

  @Bean
  fun sessionRegistry(sessionRepository: FindByIndexNameSessionRepository<*>): SessionRegistry = SpringSessionBackedSessionRegistry(sessionRepository)
}
