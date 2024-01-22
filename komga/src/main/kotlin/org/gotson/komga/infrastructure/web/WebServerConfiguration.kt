package org.gotson.komga.infrastructure.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class WebServerConfiguration(
  private val settingsProvider: KomgaSettingsProvider,
) : WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
  override fun customize(factory: ConfigurableServletWebServerFactory) {
    settingsProvider.serverPort?.let {
      if (it > 1)
        factory.setPort(it)
      else
        logger.warn { "Ignoring invalid server port: $it" }
    }
    settingsProvider.serverContextPath?.let {
      if (it.startsWith("/") && !it.endsWith("/"))
        factory.setContextPath(it)
      else
        logger.warn { "Ignoring invalid server context path: $it" }
    }
  }
}
