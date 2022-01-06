package org.gotson.komga.infrastructure.web

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.ShallowEtagHeaderFilter

@Configuration
class EtagFilterConfiguration {
  @Bean
  fun shallowEtagHeaderFilter(): FilterRegistrationBean<ShallowEtagHeaderFilter> =
    FilterRegistrationBean(ShallowEtagHeaderFilter())
      .also {
        it.addUrlPatterns(
          "/api/*",
          "/opds/*",
        )
        it.setName("etagFilter")
      }
}
