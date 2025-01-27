package org.gotson.komga.infrastructure.configuration

import org.gotson.komga.infrastructure.image.ImageType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StaticConfiguration {
  @Bean("thumbnailType")
  fun thumbnailType() = ImageType.JPEG

  @Bean("pdfImageType")
  fun pdfImageType() = ImageType.JPEG

  @Bean("pdfResolution")
  fun pdfResolution(): Float = 3200F
}
