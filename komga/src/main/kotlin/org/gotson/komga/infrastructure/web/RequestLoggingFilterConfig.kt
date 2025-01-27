package org.gotson.komga.infrastructure.web

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class RequestLoggingFilterConfig {
  @ConditionalOnProperty(value = ["logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter"], havingValue = "debug")
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
