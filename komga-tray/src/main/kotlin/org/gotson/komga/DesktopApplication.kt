package org.gotson.komga

import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
class DesktopApplication : Application()

fun main(args: Array<String>) {
  System.setProperty("apple.awt.UIElement", "true")
  System.setProperty("org.jooq.no-logo", "true")
  System.setProperty("org.jooq.no-tips", "true")
  val builder = SpringApplicationBuilder(DesktopApplication::class.java)
  builder.headless(false)
  builder.run(*args)
}
