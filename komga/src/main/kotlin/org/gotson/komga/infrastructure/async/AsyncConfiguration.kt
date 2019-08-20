package org.gotson.komga.infrastructure.async

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfiguration {

  @Bean
  fun parseBookTaskExecutor(): Executor =
      ThreadPoolTaskExecutor().apply {
        corePoolSize = 5
      }
}