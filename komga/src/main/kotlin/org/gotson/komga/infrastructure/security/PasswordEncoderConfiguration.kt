package org.gotson.komga.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.token.Sha512DigestUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoderConfiguration {
  @Bean
  fun getPasswordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun getTokenEncoder(): TokenEncoder = TokenEncoder { rawPassword -> Sha512DigestUtils.shaHex(rawPassword) }
}
