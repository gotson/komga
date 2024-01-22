package org.gotson.komga.infrastructure.security

import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class KomgaUserDetailsService(
  private val userRepository: KomgaUserRepository,
) : UserDetailsService {
  override fun loadUserByUsername(username: String): UserDetails =
    userRepository.findByEmailIgnoreCaseOrNull(username)?.let {
      KomgaPrincipal(it)
    } ?: throw UsernameNotFoundException(username)
}
