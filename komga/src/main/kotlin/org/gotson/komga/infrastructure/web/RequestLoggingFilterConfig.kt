package org.gotson.komga.infrastructure.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class RequestLoggingFilterConfig {
  @Bean
  fun logFilter() =
    CommonsRequestLoggingFilter().apply {
      setIncludeQueryString(true)
      setIncludePayload(true)
      setMaxPayloadLength(10000)
      setIncludeHeaders(true)
      setAfterMessagePrefix("REQUEST DATA: ")
    }
}
