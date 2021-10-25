package org.gotson.komga.infrastructure.security

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class UserAgentWebAuthenticationDetailsSource : WebAuthenticationDetailsSource() {
  override fun buildDetails(context: HttpServletRequest): UserAgentWebAuthenticationDetails =
    UserAgentWebAuthenticationDetails(context)
}
