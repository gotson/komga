package org.gotson.komga.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoderConfiguration {
  @Bean
  fun getEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
