package org.gotson.komga.infrastructure.async

import org.gotson.komga.infrastructure.configuration.KomgaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfiguration(
    private val komgaProperties: KomgaProperties
) {

  @Bean("analyzeBookTaskExecutor")
  fun analyzeBookTaskExecutor(): Executor =
      ThreadPoolTaskExecutor().apply {
        corePoolSize = komgaProperties.threads.analyzer
      }

  @Bean("periodicScanTaskExecutor")
  fun periodicScanTaskExecutor(): Executor =
      ThreadPoolTaskExecutor().apply {
        corePoolSize = 1
        maxPoolSize = 1
        setQueueCapacity(0)
      }

  @Bean("regenerateThumbnailsTaskExecutor")
  fun regenerateThumbnailsTaskExecutor(): Executor =
      ThreadPoolTaskExecutor().apply {
        corePoolSize = 1
        maxPoolSize = 1
        setQueueCapacity(0)
      }
}
