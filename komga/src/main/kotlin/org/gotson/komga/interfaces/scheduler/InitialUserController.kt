package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.infrastructure.security.KomgaUserDetailsLifecycle
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

@Profile("dev", "prod")
@Controller
class InitialUserController(
    private val userDetailsLifecycle: KomgaUserDetailsLifecycle,
    private val initialUserPassword: String
) {

  @EventListener(ApplicationReadyEvent::class)
  fun createInitialUserOnStartupIfNoneExist() {
    if (userDetailsLifecycle.countUsers() == 0L) {
      logger.info { "No users exist in database, creating an initial user" }

      val initialUser = User
          .withUsername("admin@example.org")
          .password(initialUserPassword)
          .roles(UserRoles.ADMIN.name)
          .build()

      userDetailsLifecycle.createUser(initialUser)

      logger.info { "Initial user created. Login: ${initialUser.username}, Password: ${initialUser.password}" }
    }
  }
}

@Configuration
@Profile("dev")
class InitialUserPasswordDevConfiguration {
  @Bean
  fun initialUserPassword() = "admin"
}

@Configuration
@Profile("prod")
class InitialUserPasswordProdConfiguration {
  @Bean
  fun initialUserPassword(): String = RandomStringUtils.randomAlphanumeric(12)
}
