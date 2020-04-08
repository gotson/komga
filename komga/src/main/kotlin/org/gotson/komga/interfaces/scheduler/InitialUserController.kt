package org.gotson.komga.interfaces.scheduler

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.gotson.komga.domain.model.KomgaUser
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

@Profile("!(test | claim)")
@Controller
class InitialUserController(
  private val userDetailsLifecycle: KomgaUserDetailsLifecycle,
  private val initialUsers: List<KomgaUser>
) {

  @EventListener(ApplicationReadyEvent::class)
  fun createInitialUserOnStartupIfNoneExist() {
    if (userDetailsLifecycle.countUsers() == 0L) {
      logger.info { "No users exist in database, creating initial users" }

      initialUsers
        .map {
          User.withUsername(it.email)
            .password(it.password)
            .roles(*it.roles.map { it.name }.toTypedArray())
            .build()
        }.forEach {
          userDetailsLifecycle.createUser(it)
          logger.info { "Initial user created. Login: ${it.username}, Password: ${it.password}" }
        }
    }
  }
}

@Configuration
@Profile("dev")
class InitialUsersDevConfiguration {
  @Bean
  fun initialUsers() = listOf(
    KomgaUser("admin@example.org", "admin", mutableSetOf(UserRoles.ADMIN)),
    KomgaUser("user@example.org", "user")
  )
}

@Configuration
@Profile("!dev")
class InitialUsersProdConfiguration {
  @Bean
  fun initialUsers() = listOf(
    KomgaUser("admin@example.org", RandomStringUtils.randomAlphanumeric(12), mutableSetOf(UserRoles.ADMIN))
  )
}
