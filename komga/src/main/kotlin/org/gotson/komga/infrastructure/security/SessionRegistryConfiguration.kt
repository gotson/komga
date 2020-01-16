package org.gotson.komga.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl

@Configuration
class SessionRegistryConfiguration {
  @Bean
  fun sessionRegistry(): SessionRegistry {
    return SessionRegistryImpl()
  }
}
