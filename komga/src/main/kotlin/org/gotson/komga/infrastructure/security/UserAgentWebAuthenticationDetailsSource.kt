package org.gotson.komga.infrastructure.security

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import javax.servlet.http.HttpServletRequest

class UserAgentWebAuthenticationDetailsSource : WebAuthenticationDetailsSource() {
  override fun buildDetails(context: HttpServletRequest): UserAgentWebAuthenticationDetails =
    UserAgentWebAuthenticationDetails(context)
}
