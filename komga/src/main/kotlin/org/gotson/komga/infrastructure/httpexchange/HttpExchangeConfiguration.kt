package org.gotson.komga.infrastructure.httpexchange

import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpExchangeConfiguration {
  private val httpExchangeRepository = InMemoryHttpExchangeRepository()

  @Bean
  fun httpExchangeRepository() = httpExchangeRepository
}
