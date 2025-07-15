package org.gotson.komga.application.gui

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import org.gotson.komga.RB
import org.gotson.komga.infrastructure.web.WebServerEffectiveSettings
import org.gotson.komga.openExplorer
import org.gotson.komga.openUrl
import org.jetbrains.compose.resources.decodeToSvgPainter
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
  @param:Value("#{komgaProperties.configDir}") komgaConfigDir: String,
  @param:Value($$"${logging.file.name}") logFileName: String,
  serverSettings: WebServerEffectiveSettings,
  env: Environment,
) : ApplicationRunner {
  val komgaUrl by lazy { "http://localhost:${serverSettings.effectiveServerPort}${serverSettings.effectiveServletContextPath}" }
  val komgaConfigDir by lazy { File(komgaConfigDir) }
  val logFile by lazy { File(logFileName) }
  val iconFileName = if (env.activeProfiles.contains("mac")) "komga-gray-minimal.svg" else "komga-color.svg"

  @Async
  override fun run(args: ApplicationArguments) {
    runTray()
  }

  private fun runTray() {
    application {
      Tray(
        icon = ClassPathResource("icons/$iconFileName").inputStream.readAllBytes().decodeToSvgPainter(LocalDensity.current),
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
