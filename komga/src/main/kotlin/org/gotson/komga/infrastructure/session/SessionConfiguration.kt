package org.gotson.komga.infrastructure.session

import org.springframework.boot.autoconfigure.web.ServerProperties
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
  fun sessionHeaderName() = "X-Auth-Token"

  @Bean
  fun cookieSerializer(serverProperties: ServerProperties): CookieSerializer =
    DefaultCookieSerializer().apply {
      serverProperties.servlet.session.cookie.let { cookieProperties ->
        cookieProperties.name?.let { setCookieName(it) }
        cookieProperties.domain?.let { setDomainName(it) }
        cookieProperties.path?.let { setCookiePath(it) }
        cookieProperties.httpOnly?.let { setUseHttpOnlyCookie(it) }
        cookieProperties.secure?.let { setUseSecureCookie(it) }
        cookieProperties.maxAge?.let { setCookieMaxAge(it.seconds.toInt()) }
        cookieProperties.sameSite?.let { setSameSite(it.toString()) }
      }
    }

  @Bean
  fun httpSessionIdResolver(
    sessionHeaderName: String,
    cookieSerializer: CookieSerializer,
  ): HttpSessionIdResolver = SmartHttpSessionIdResolver(sessionHeaderName, cookieSerializer)

  @Bean
  fun sessionRegistry(sessionRepository: FindByIndexNameSessionRepository<*>): SessionRegistry = SpringSessionBackedSessionRegistry(sessionRepository)
}
