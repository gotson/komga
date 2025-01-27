package org.gotson.komga.infrastructure.metadata.barcode

import org.apache.commons.validator.routines.ISBNValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IsbnConfiguration {
  @Bean
  fun isbnValidator() = ISBNValidator(true)
}
