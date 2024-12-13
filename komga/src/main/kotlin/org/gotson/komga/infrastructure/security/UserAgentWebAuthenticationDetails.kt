package org.gotson.komga.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.security.web.authentication.WebAuthenticationDetails

class UserAgentWebAuthenticationDetails(
  request: HttpServletRequest,
) : WebAuthenticationDetails(request) {
  val userAgent: String = request.getHeader(HttpHeaders.USER_AGENT).orEmpty()
}
