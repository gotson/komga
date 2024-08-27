package org.gotson.komga.infrastructure.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.PathContainer
import org.springframework.web.filter.ShallowEtagHeaderFilter
import org.springframework.web.util.pattern.PathPatternParser

@Configuration
class EtagFilterConfiguration {
  private val excludePatterns =
    listOf(
      PathPatternParser.defaultInstance.parse("/api/v1/books/*/file/**"),
      PathPatternParser.defaultInstance.parse("/opds/v1.2/books/*/file/**"),
      PathPatternParser.defaultInstance.parse("/api/v1/readlists/*/file/**"),
      PathPatternParser.defaultInstance.parse("/api/v1/series/*/file/**"),
      PathPatternParser.defaultInstance.parse("/kobo/*/v1/books/*/file/**"),
    )

  @Bean
  fun shallowEtagHeaderFilter(): FilterRegistrationBean<out ShallowEtagHeaderFilter> =
    FilterRegistrationBean(
      object : ShallowEtagHeaderFilter() {
        override fun shouldNotFilter(request: HttpServletRequest): Boolean {
          val path = PathContainer.parsePath(request.servletPath)
          return excludePatterns.any { it.matches(path) }
        }
      },
    ).also {
      it.addUrlPatterns(
        "/api/*",
        "/opds/*",
        "/kobo/*",
      )
      it.setName("etagFilter")
    }
}
