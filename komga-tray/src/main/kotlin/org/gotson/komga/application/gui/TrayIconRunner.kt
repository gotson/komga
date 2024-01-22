package org.gotson.komga.application.gui

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import org.gotson.komga.RB
import org.gotson.komga.openExplorer
import org.gotson.komga.openUrl
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File

@Profile("!test")
@Component
class TrayIconRunner(
  @Value("#{servletContext.contextPath}") servletContextPath: String,
  @Value("#{komgaProperties.configDir}") komgaConfigDir: String,
  @Value("\${logging.file.name}") logFileName: String,
  @Value("\${server.port}") serverPort: Int,
  env: Environment,
) : ApplicationRunner {
  val komgaUrl = "http://localhost:$serverPort$servletContextPath"
  val komgaConfigDir = File(komgaConfigDir)
  val logFile = File(logFileName)
  val iconFileName = if (env.activeProfiles.contains("mac")) "komga-gray-minimal.svg" else "komga-color.svg"

  @Async
  override fun run(args: ApplicationArguments) {
    runTray()
  }

  private fun runTray() {
    application {
      Tray(
        icon = loadSvgPainter(ClassPathResource("icons/$iconFileName").inputStream, LocalDensity.current),
        menu = {
          Item(RB.getString("menu.open_komga"), onClick = { openUrl(komgaUrl) })
          Item(RB.getString("menu.show_log"), onClick = { openExplorer(logFile) })
          Item(RB.getString("menu.show_conf_dir"), onClick = { openExplorer(komgaConfigDir) })
          Item(RB.getString("menu.quit"), onClick = ::exitApplication)
        },
      )
    }
  }
}
