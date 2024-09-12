package org.gotson.komga.infrastructure.web

import jakarta.annotation.PostConstruct
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
class KoboMissingPortFilterConfiguration(
  private val komgaSettingsProvider: KomgaSettingsProvider,
  private val serverSettings: WebServerEffectiveSettings,
  private val forwardedHeaderFilter: FilterRegistrationBean<ForwardedHeaderFilter>?,
) {
  @Bean
  fun koboMissingPortFilter(): FilterRegistrationBean<out KoboMissingPortFilter> =
    FilterRegistrationBean(
      KoboMissingPortFilter { komgaSettingsProvider.koboPort ?: serverSettings.effectiveServerPort },
    ).apply {
      addUrlPatterns(
        "/kobo/*",
      )
      setName("koboMissingPortFilter")
      order = Ordered.HIGHEST_PRECEDENCE
    }

  @PostConstruct
  fun adjustForwardHeaderFilterOrder() {
    // the ForwardHeaderFilter must be after the KoboMissingPortFilter, as the latter's detection is based on forwarded headers
    // that the former will remove
    forwardedHeaderFilter?.order = Ordered.HIGHEST_PRECEDENCE + 1
  }
}
