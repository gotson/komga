package org.gotson.komga.infrastructure.security

import org.springframework.http.HttpHeaders
import org.springframework.security.web.authentication.WebAuthenticationDetails
import javax.servlet.http.HttpServletRequest

class UserAgentWebAuthenticationDetails(request: HttpServletRequest) : WebAuthenticationDetails(request) {
  val userAgent: String = request.getHeader(HttpHeaders.USER_AGENT).orEmpty()
}
