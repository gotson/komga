package org.gotson.komga.application.events

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class AsynchronousSpringEventsConfig(
  private val taskExecutor: ThreadPoolTaskExecutor,
) {
  @Bean("applicationEventMulticaster")
  fun simpleApplicationEventMulticaster(): ApplicationEventMulticaster {
    val eventMulticaster = SimpleApplicationEventMulticaster()
    eventMulticaster.setTaskExecutor(taskExecutor)
    return eventMulticaster
  }
}
