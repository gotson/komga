package org.gotson.komga.infrastructure.security

import org.gotson.komga.domain.model.ApiKey
import org.gotson.komga.domain.model.KomgaUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User

class KomgaPrincipal(
  val user: KomgaUser,
  val oAuth2User: OAuth2User? = null,
  val oidcUser: OidcUser? = null,
  val apiKey: ApiKey? = null,
  private val name: String = user.email,
) : UserDetails,
  OAuth2User,
  OidcUser {
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
    user.roles
      .map { SimpleGrantedAuthority("ROLE_$it") }
      .toMutableSet()

  override fun isEnabled() = true

  override fun getUsername() = name

  override fun isCredentialsNonExpired() = true

  override fun getPassword() = user.password

  override fun isAccountNonExpired() = true

  override fun isAccountNonLocked() = true

  override fun getName() = name

  override fun getAttributes(): MutableMap<String, Any> = oAuth2User?.attributes ?: mutableMapOf()

  override fun getClaims(): MutableMap<String, Any> = oidcUser?.claims ?: mutableMapOf()

  override fun getUserInfo(): OidcUserInfo? = oidcUser?.userInfo

  override fun getIdToken(): OidcIdToken? = oidcUser?.idToken
}
