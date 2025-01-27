package org.gotson.komga.interfaces.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test & noclaim")
@Component
class InitialUserController(
  private val userLifecycle: KomgaUserLifecycle,
  private val initialUsers: List<KomgaUser>,
) {
  @EventListener(ApplicationReadyEvent::class)
  fun createInitialUserOnStartupIfNoneExist() {
    if (userLifecycle.countUsers() == 0L) {
      logger.info { "No users exist in database, creating initial users" }

      initialUsers
        .forEach {
          userLifecycle.createUser(it)
          logger.info { "Initial user created. Login: ${it.email}, Password: ${it.password}" }
        }
    }
  }
}

@Configuration
@Profile("dev")
class InitialUsersDevConfiguration {
  @Bean
  fun initialUsers(): List<KomgaUser> =
    listOf(
      KomgaUser("admin@example.org", "admin", roles = UserRoles.entries.toSet()),
      KomgaUser("user@example.org", "user"),
    )
}

@Configuration
@Profile("!dev")
class InitialUsersProdConfiguration {
  @Bean
  fun initialUsers(): List<KomgaUser> =
    listOf(
      KomgaUser("admin@example.org", RandomStringUtils.secure().nextAlphanumeric(12), roles = UserRoles.entries.toSet()),
    )
}
