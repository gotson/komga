package org.gotson.komga.interfaces.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.persistence.AuthenticationActivityRepository
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class AuthenticationActivityCleanupController(
  private val authenticationActivityRepository: AuthenticationActivityRepository,
) {
  // Run every day
  @Scheduled(fixedRate = 86_400_000)
  fun cleanup() {
    val olderThan = LocalDateTime.now(ZoneId.of("Z")).minusMonths(1)
    logger.info { "Remove authentication activity older than $olderThan (UTC)" }
    authenticationActivityRepository.deleteOlderThan(olderThan)
  }
}
