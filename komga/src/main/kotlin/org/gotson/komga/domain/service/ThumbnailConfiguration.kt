package org.gotson.komga.domain.service

import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ThumbnailConfiguration {
  @Bean
  fun thumbnailType() = ImageType.JPEG
}
