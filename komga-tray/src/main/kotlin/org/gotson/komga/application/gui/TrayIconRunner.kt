package org.gotson.komga.application.gui

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import org.gotson.komga.openUrl
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Profile("!test")
@Component
class TrayIconRunner(
  @Value("#{servletContext.contextPath}") servletContextPath: String,
  @Value("\${server.port}") serverPort: Int,
  env: Environment,
) : ApplicationRunner {

  val komgaUrl = "http://localhost:$serverPort$servletContextPath"
  val iconFileName = if (env.activeProfiles.contains("mac")) "komga-gray-minimal.svg" else "komga-color.svg"
  override fun run(args: ApplicationArguments) {
    runTray()
  }

  private fun runTray() {
    application {
      Tray(
        icon = loadSvgPainter(ClassPathResource("icons/$iconFileName").inputStream, LocalDensity.current),
        menu = {
          Item("Open Komga", onClick = { openUrl(komgaUrl) })
          Item("Quit Komga", onClick = ::exitApplication)
        },
      )
    }
  }
}
