package org.gotson.komga.infrastructure.security

import mu.KotlinLogging
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsPasswordService
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class KomgaUserDetailsLifecycle(
    private val userRepository: KomgaUserRepository,
    private val passwordEncoder: PasswordEncoder

) : UserDetailsService, UserDetailsPasswordService {

  override fun loadUserByUsername(username: String): UserDetails =
      userRepository.findByEmail(username)?.let {
        KomgaPrincipal(it)
      } ?: throw UsernameNotFoundException(username)

  @Transactional
  override fun updatePassword(user: UserDetails, newPassword: String): UserDetails {
    userRepository.findByEmail(user.username)?.let { komgaUser ->
      komgaUser.password = passwordEncoder.encode(newPassword)
      userRepository.save(komgaUser)
      logger.info { "Changed password for user ${user.username}" }
      return KomgaPrincipal(komgaUser)
    } ?: throw UsernameNotFoundException(user.username)
  }

  fun countUsers() = userRepository.count()

  @Transactional
  @Throws(UserEmailAlreadyExistsException::class)
  fun createUser(user: UserDetails): UserDetails {
    if (userRepository.existsByEmail(user.username)) throw UserEmailAlreadyExistsException("A user with the same email already exists: ${user.username}")

    val komgaUser = KomgaUser(
        email = user.username,
        password = passwordEncoder.encode(user.password),
        roles = user.authorities.toUserRoles()
    )

    userRepository.save(komgaUser)
    logger.info { "Created user: ${komgaUser.email}, roles: ${komgaUser.roles}" }
    return KomgaPrincipal(komgaUser)
  }

}

private fun Iterable<GrantedAuthority>.toUserRoles() =
    this.filter { it.authority.startsWith("ROLE_") }
        .map { it.authority.removePrefix("ROLE_") }
        .mapNotNull {
          try {
            UserRoles.valueOf(it)
          } catch (e: Exception) {
            null
          }
        }
        .toMutableSet()

class UserEmailAlreadyExistsException(message: String) : Exception(message)
