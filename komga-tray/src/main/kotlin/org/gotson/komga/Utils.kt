package org.gotson.komga

import java.awt.Desktop
import java.io.File
import java.net.URI

fun openUrl(url: String) {
  if (Desktop.isDesktopSupported())
    Desktop.getDesktop().let {
      if (it.isSupported(Desktop.Action.BROWSE)) it.browse(URI.create(url))
    }
}

fun openExplorer(file: File) {
  if (Desktop.isDesktopSupported())
    Desktop.getDesktop().let {
      if (it.isSupported(Desktop.Action.BROWSE_FILE_DIR)) it.browseFileDirectory(file)
    }
}
