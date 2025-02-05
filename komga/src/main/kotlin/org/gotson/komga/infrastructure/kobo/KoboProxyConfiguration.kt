package org.gotson.komga.infrastructure.kobo

import com.fasterxml.jackson.databind.ObjectMapper
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KoboConfig {

  @Bean
  @Qualifier("koboStoreApiProxy")
  fun storeApiBaseUrl(): String = "https://storeapi.kobo.com"

  @Bean
  @Qualifier("readingServicesBaseUrl")
  fun readingServicesBaseUrl(): String = "https://readingservices.kobo.com"

  @Bean
  fun koboReadingservicesProxy(
    objectMapper: ObjectMapper,
    komgaSyncTokenGenerator: KomgaSyncTokenGenerator,
    komgaSettingsProvider: KomgaSettingsProvider,
    @Qualifier("readingServicesBaseUrl") baseUrl: String
  ): KoboProxy {
    return KoboProxy(objectMapper, komgaSyncTokenGenerator, komgaSettingsProvider, baseUrl)
  }

  @Bean
  fun koboStoreApiProxy(
    objectMapper: ObjectMapper,
    komgaSyncTokenGenerator: KomgaSyncTokenGenerator,
    komgaSettingsProvider: KomgaSettingsProvider,
    @Qualifier("storeApiBaseUrl") baseUrl: String
  ): KoboProxy {
    return KoboProxy(objectMapper, komgaSyncTokenGenerator, komgaSettingsProvider, baseUrl)
  }
}
