package org.gotson.komga

import org.gotson.komga.infrastructure.util.checkTempDirectory
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
class DesktopApplication : Application()

fun main(args: Array<String>) {
  checkTempDirectory()

  System.setProperty("apple.awt.UIElement", "true")
  System.setProperty("org.jooq.no-logo", "true")
  System.setProperty("org.jooq.no-tips", "true")

  SpringApplicationBuilder(DesktopApplication::class.java).apply {
    headless(false)
    run(*args)
  }
}
