package org.gotson.komga.infrastructure.web

import jakarta.servlet.ServletContext
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class WebServerEffectiveSettings(
  servletContext: ServletContext,
) {
  var effectiveServerPort: Int? = null
  val effectiveServletContextPath: String = servletContext.contextPath

  @EventListener
  fun onApplicationEvent(event: ServletWebServerInitializedEvent) {
    effectiveServerPort = event.webServer.port
  }
}
