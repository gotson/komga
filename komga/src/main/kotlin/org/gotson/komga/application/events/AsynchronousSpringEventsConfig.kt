package org.gotson.komga.application.events

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.task.AsyncTaskExecutor

@Configuration
class AsynchronousSpringEventsConfig(
  private val applicationTaskExecutor: AsyncTaskExecutor,
) {
  @Bean("applicationEventMulticaster")
  fun simpleApplicationEventMulticaster(): ApplicationEventMulticaster =
    SimpleApplicationEventMulticaster().apply {
      setTaskExecutor(applicationTaskExecutor)
    }
}
