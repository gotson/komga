package org.gotson.komga.infrastructure.session

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.session.SessionRegistry
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
import org.springframework.session.web.http.HttpSessionIdResolver

@Configuration
class SessionConfiguration {
  @Bean
  fun sessionCookieName() = "KOMGA-SESSION"

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
  fun sessionRegistry(sessionRepository: FindByIndexNameSessionRepository<*>): SessionRegistry = SpringSessionBackedSessionRegistry(sessionRepository)
}
