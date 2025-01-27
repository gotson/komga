package org.gotson.komga.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component

@Component
class UserAgentWebAuthenticationDetailsSource : WebAuthenticationDetailsSource() {
  override fun buildDetails(context: HttpServletRequest): UserAgentWebAuthenticationDetails = UserAgentWebAuthenticationDetails(context)
}
