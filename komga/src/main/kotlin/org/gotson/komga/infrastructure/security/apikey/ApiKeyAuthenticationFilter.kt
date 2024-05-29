package org.gotson.komga.infrastructure.security.apikey

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.filter.OncePerRequestFilter

class ApiKeyAuthenticationFilter(
  private val authenticationManager: AuthenticationManager,
  private val authenticationConverter: AuthenticationConverter,
) : OncePerRequestFilter() {
  private val securityContextHolderStrategy: SecurityContextHolderStrategy =
    SecurityContextHolder
      .getContextHolderStrategy()

  private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()

  private val failureHandler: AuthenticationFailureHandler =
    AuthenticationEntryPointFailureHandler(
      HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
    )

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    try {
      val authRequest = authenticationConverter.convert(request)
      if (authRequest == null) {
        filterChain.doFilter(request, response)
        return
      }
      if (authenticationIsRequired(authRequest.name)) {
        val authResult = authenticationManager.authenticate(authRequest)
        if (authResult == null) {
          filterChain.doFilter(request, response)
          return
        }
        successfulAuthentication(request, response, filterChain, authResult)
      }
    } catch (ex: AuthenticationException) {
      unsuccessfulAuthentication(request, response, ex)
    }
  }

  private fun unsuccessfulAuthentication(
    request: HttpServletRequest,
    response: HttpServletResponse,
    failed: AuthenticationException,
  ) {
    securityContextHolderStrategy.clearContext()
    failureHandler.onAuthenticationFailure(request, response, failed)
  }

  private fun successfulAuthentication(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
    authentication: Authentication,
  ) {
    val context =
      securityContextHolderStrategy.createEmptyContext().apply {
        this.authentication = authentication
      }
    securityContextHolderStrategy.context = context
    securityContextRepository.saveContext(context, request, response)
    filterChain.doFilter(request, response)
  }

  private fun authenticationIsRequired(username: String): Boolean {
    // Only reauthenticate if username doesn't match SecurityContextHolder and user isn't authenticated
    val existingAuth = this.securityContextHolderStrategy.context.authentication
    if (existingAuth == null || existingAuth.name != username || !existingAuth.isAuthenticated) {
      return true
    }
    // Handle unusual condition where an AnonymousAuthenticationToken is already
    // present. This shouldn't happen very often, as ApiKeyAuthenticationFilter is
    // meant to be earlier in the filter chain than AnonymousAuthenticationFilter.
    // Also check that the existing token is of type ApiKeyAuthenticationToken.
    // This would prevent reusing a session obtained from Basic Auth for example.
    return (existingAuth is AnonymousAuthenticationToken || existingAuth !is ApiKeyAuthenticationToken)
  }
}
