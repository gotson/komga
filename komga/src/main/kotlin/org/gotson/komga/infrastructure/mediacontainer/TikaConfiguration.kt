package org.gotson.komga.infrastructure.mediacontainer

import org.apache.tika.config.TikaConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TikaConfiguration {
  @Bean
  fun tika() = TikaConfig()
}
