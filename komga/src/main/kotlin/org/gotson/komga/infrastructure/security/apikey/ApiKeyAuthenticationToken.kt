package org.gotson.komga.infrastructure.security.apikey

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * A specialization of [UsernamePasswordAuthenticationToken] to store API keys.
 */
class ApiKeyAuthenticationToken private constructor(principal: Any?, credentials: Any?, authorities: Collection<GrantedAuthority>?) : UsernamePasswordAuthenticationToken(principal, credentials, authorities) {
  private constructor(principal: Any?, credentials: Any?) : this(principal, credentials, null) {
    isAuthenticated = false
  }

  companion object {
    fun authenticated(
      principal: Any?,
      credentials: Any?,
      authorities: Collection<GrantedAuthority>?,
    ) = ApiKeyAuthenticationToken(principal, credentials, authorities)

    fun unauthenticated(
      principal: Any?,
      credentials: Any?,
    ) = ApiKeyAuthenticationToken(principal, credentials)
  }
}
