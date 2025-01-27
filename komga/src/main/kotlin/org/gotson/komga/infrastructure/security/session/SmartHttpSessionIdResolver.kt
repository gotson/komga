package org.gotson.komga.infrastructure.security.session

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.session.web.http.CookieHttpSessionIdResolver
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.HeaderHttpSessionIdResolver
import org.springframework.session.web.http.HttpSessionIdResolver

class SmartHttpSessionIdResolver(
  private val sessionHeaderName: String,
  cookieSerializer: CookieSerializer,
) : HttpSessionIdResolver {
  private val cookie = CookieHttpSessionIdResolver().apply { setCookieSerializer(cookieSerializer) }
  private val header = HeaderHttpSessionIdResolver(sessionHeaderName)

  override fun resolveSessionIds(request: HttpServletRequest): List<String> = request.getResolver().resolveSessionIds(request)

  override fun setSessionId(
    request: HttpServletRequest,
    response: HttpServletResponse,
    sessionId: String,
  ) {
    request.getResolver().setSessionId(request, response, sessionId)
  }

  override fun expireSession(
    request: HttpServletRequest,
    response: HttpServletResponse,
  ) {
    request.getResolver().expireSession(request, response)
  }

  private fun HttpServletRequest.getResolver() = if (this.getHeader(sessionHeaderName) != null) header else cookie
}
