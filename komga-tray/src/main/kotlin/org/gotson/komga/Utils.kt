package org.gotson.komga

import mu.KotlinLogging
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
  if (Desktop.isDesktopSupported())
    Desktop.getDesktop().let {
      if (it.isSupported(Desktop.Action.BROWSE_FILE_DIR)) it.browseFileDirectory(file)
      else logger.warn { "Cannot open explorer: Desktop.Action.BROWSE_FILE_DIR is not supported" }
    }
  else
    logger.warn { "Cannot open explorer: Desktop is not supported" }
}
