package org.gotson.komga.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gotson.komga.domain.model.DomainEvent
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserEmailAlreadyExistsException
import org.gotson.komga.domain.persistence.AuthenticationActivityRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.ReadProgressRepository
import org.gotson.komga.domain.persistence.SyncPointRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class KomgaUserLifecycle(
  private val userRepository: KomgaUserRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val authenticationActivityRepository: AuthenticationActivityRepository,
  private val syncPointRepository: SyncPointRepository,
  private val passwordEncoder: PasswordEncoder,
  private val sessionRegistry: SessionRegistry,
  private val transactionTemplate: TransactionTemplate,
  private val eventPublisher: ApplicationEventPublisher,
) {
  fun updatePassword(
    user: KomgaUser,
    newPassword: String,
    expireSessions: Boolean,
  ) {
    logger.info { "Changing password for user ${user.email}" }
    val updatedUser = user.copy(password = passwordEncoder.encode(newPassword))
    userRepository.update(updatedUser)

    if (expireSessions) expireSessions(updatedUser)

    eventPublisher.publishEvent(DomainEvent.UserUpdated(updatedUser, expireSessions))
  }

  fun updateUser(user: KomgaUser) {
    val existing = userRepository.findByIdOrNull(user.id)
    requireNotNull(existing) { "User doesn't exist, cannot update: $user" }

    val toUpdate = user.copy(password = existing.password)
    logger.info { "Update user: $toUpdate" }
    userRepository.update(toUpdate)

    val expireSessions =
      existing.roles != user.roles ||
        existing.restrictions != user.restrictions ||
        existing.sharedAllLibraries != user.sharedAllLibraries ||
        existing.sharedLibrariesIds != user.sharedLibrariesIds

    if (expireSessions) expireSessions(toUpdate)

    eventPublisher.publishEvent(DomainEvent.UserUpdated(toUpdate, expireSessions))
  }

  fun countUsers() = userRepository.count()

  @Throws(UserEmailAlreadyExistsException::class)
  fun createUser(komgaUser: KomgaUser): KomgaUser {
    if (userRepository.existsByEmailIgnoreCase(komgaUser.email)) throw UserEmailAlreadyExistsException("A user with the same email already exists: ${komgaUser.email}")

    userRepository.insert(komgaUser.copy(password = passwordEncoder.encode(komgaUser.password)))

    val createdUser = userRepository.findByIdOrNull(komgaUser.id)!!
    logger.info { "User created: $createdUser" }
    return createdUser
  }

  fun deleteUser(user: KomgaUser) {
    logger.info { "Deleting user: $user" }

    transactionTemplate.executeWithoutResult {
      readProgressRepository.deleteByUserId(user.id)
      authenticationActivityRepository.deleteByUser(user)
      syncPointRepository.deleteByUserId(user.id)
      userRepository.delete(user.id)
    }

    expireSessions(user)

    eventPublisher.publishEvent(DomainEvent.UserUpdated(user, true))
  }

  fun expireSessions(user: KomgaUser) {
    logger.info { "Expiring all sessions for user: ${user.email}" }
    sessionRegistry
      .getAllSessions(KomgaPrincipal(user), false)
      .forEach {
        logger.info { "Expiring session: ${it.sessionId}" }
        it.expireNow()
      }
  }
}
