package org.gotson.komga

import org.gotson.komga.cli.ImagesMarker
import org.gotson.komga.infrastructure.util.checkTempDirectory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
  // cli handlers
  if(args.firstOrNull() == "images_mark") return ImagesMarker.handler(args)

  checkTempDirectory()

  System.setProperty("org.jooq.no-logo", "true")
  System.setProperty("org.jooq.no-tips", "true")

  runApplication<Application>(*args)
}
