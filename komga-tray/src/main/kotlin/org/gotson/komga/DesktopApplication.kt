package org.gotson.komga

import org.springframework.boot.builder.SpringApplicationBuilder

fun main(args: Array<String>) {
  System.setProperty("apple.awt.UIElement", "true")
  System.setProperty("org.jooq.no-logo", "true")
  System.setProperty("org.jooq.no-tips", "true")
  val builder = SpringApplicationBuilder(Application::class.java)
  builder.headless(false)
  builder.run(*args)
}
