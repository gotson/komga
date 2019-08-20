package org.gotson.komga

import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(
    KomgaProperties::class
)
@EnableScheduling
@EnableJpaAuditing
class Application

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}
