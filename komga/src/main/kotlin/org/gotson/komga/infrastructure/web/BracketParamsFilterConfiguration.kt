package org.gotson.komga.infrastructure.web

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

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
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
      chain?.doFilter(BracketParamsRequestWrapper(request as HttpServletRequest), response)
    }
  }
}
