package org.gotson.komga

import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger {}

fun openUrl(url: String) {
  if (Desktop.isDesktopSupported())
    Desktop.getDesktop().let {
      if (it.isSupported(Desktop.Action.BROWSE)) it.browse(URI.create(url))
    }
}

fun openExplorer(file: File) {
  logger.info { "Try to open explorer for path: $file" }
  when {
    Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR) -> Desktop.getDesktop().browseFileDirectory(file)
    System.getProperty("os.name").startsWith("win", true) -> {
      val command = """explorer.exe /select, "${file.absolutePath}""""
      Runtime.getRuntime().exec(command)
    }

    else -> logger.warn { "Cannot open explorer, not supported" }
  }
}
