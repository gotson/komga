package org.gotson.komga.infrastructure.httptrace

import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpTraceConfiguration {

  private val httpTraceRepository = InMemoryHttpTraceRepository()

  @Bean
  fun httpTraceRepository() = httpTraceRepository
}
