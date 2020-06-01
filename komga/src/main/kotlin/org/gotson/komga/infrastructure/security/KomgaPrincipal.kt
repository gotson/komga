package org.gotson.komga.infrastructure.security

import org.gotson.komga.domain.model.KomgaUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class KomgaPrincipal(
  val user: KomgaUser
) : UserDetails {

  override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
    user.roles()
      .map { SimpleGrantedAuthority("ROLE_$it") }
      .toMutableSet()

  override fun isEnabled() = true

  override fun getUsername() = user.email

  override fun isCredentialsNonExpired() = true

  override fun getPassword() = user.password

  override fun isAccountNonExpired() = true

  override fun isAccountNonLocked() = true
}
