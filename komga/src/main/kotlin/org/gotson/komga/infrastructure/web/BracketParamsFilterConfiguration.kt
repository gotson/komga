package org.gotson.komga.infrastructure.web

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BracketParamsFilterConfiguration {
  @Bean
  fun bracketParamsFilter(): FilterRegistrationBean<BracketParamsFilter> =
    FilterRegistrationBean(BracketParamsFilter())
      .also {
        it.addUrlPatterns(
          "/api/*",
        )
        it.setName("queryParamsFilter")
      }

  inner class BracketParamsFilter : Filter {
    override fun doFilter(
      request: ServletRequest?,
      response: ServletResponse?,
      chain: FilterChain?,
    ) {
      chain?.doFilter(BracketParamsRequestWrapper(request as HttpServletRequest), response)
    }
  }
}
