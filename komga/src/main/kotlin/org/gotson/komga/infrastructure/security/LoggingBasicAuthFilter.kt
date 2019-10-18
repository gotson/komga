package org.gotson.komga.infrastructure.security

import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = KotlinLogging.logger {}

class LoggingBasicAuthFilter(
    authenticationManager: AuthenticationManager
) : BasicAuthenticationFilter(authenticationManager) {

  override fun onUnsuccessfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, failed: AuthenticationException) {
    val cause = when {
      failed is BadCredentialsException -> "Bad credentials"
      !failed.message.isNullOrBlank() -> failed.message!!
      else -> failed.toString()
    }

    log.info { "Authentication failure: $cause, ${request.extractInfo()}" }
  }

  override fun onSuccessfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, authResult: Authentication) {
    val user = when (val p = authResult.principal) {
      is KomgaPrincipal -> p.user.email
      is Principal -> p.name
      else -> p.toString()
    }

    log.info { "Authentication success for user: $user, ${request.extractInfo()}" }
  }
}

data class RequestInfo(
    val ip: String,
    val userAgent: String?,
    val method: String,
    val url: String,
    val query: String?
)

fun HttpServletRequest.extractInfo() = RequestInfo(
    ip = remoteAddr,
    userAgent = getHeader("User-Agent"),
    method = method,
    url = requestURL.toString(),
    query = queryString
)
