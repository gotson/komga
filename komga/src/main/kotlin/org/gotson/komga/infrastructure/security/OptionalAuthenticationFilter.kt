package org.gotson.komga.infrastructure.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.gotson.komga.domain.model.AnonymousUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

/**
 * Filter that provides anonymous authentication when no credentials are present.
 * Allows optional authentication for API endpoints.
 */
@Component
class OptionalAuthenticationFilter(
  private val realIpResolver: RealIpResolver,
) : OncePerRequestFilter() {
  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    // Check if user is already authenticated
    val existingAuth = SecurityContextHolder.getContext().authentication

    if (existingAuth == null || !existingAuth.isAuthenticated) {
      // Check if this is a public endpoint
      if (isPublicEndpoint(request)) {
        // Get real IP address from reverse proxy headers
        val realIp = request.getRealIpAddress()
        val sessionId = request.session?.id

        logger.debug { "Creating anonymous user for IP: $realIp, session: $sessionId" }

        // Create anonymous user authentication with real IP tracking
        val anonymousUser = AnonymousUser.asKomgaUser()
        val principal = KomgaPrincipal(anonymousUser)

        val authorities = listOf(
          SimpleGrantedAuthority("ROLE_ANONYMOUS"),
          SimpleGrantedAuthority("ROLE_PAGE_STREAMING"),
        )

        val authentication = UsernamePasswordAuthenticationToken(
          principal,
          null,
          authorities,
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

        SecurityContextHolder.getContext().authentication = authentication

        logger.debug { "Granted anonymous access to ${request.requestURI} from $realIp" }
      }
    }

    filterChain.doFilter(request, response)
  }

  /**
   * Check if the endpoint allows anonymous access.
   */
  private fun isPublicEndpoint(request: HttpServletRequest): Boolean {
    val uri = request.requestURI
    val method = request.method

    // Public API endpoints (read-only access)
    val publicPaths = listOf(
      "/api/v1/books",
      "/api/v1/series",
      "/api/v1/libraries",
      "/api/v1/collections",
      "/api/v1/readlists",
      "/api/v1/languages",
      "/api/v1/reader/modes",
      "/opds/v1.2/catalog",
      "/opds/v2/catalog",
    )

    // Only GET requests allowed for anonymous users
    if (method != "GET" && method != "HEAD") {
      return false
    }

    // Check if path matches public endpoints
    return publicPaths.any { uri.startsWith(it) }
  }

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    // Always run this filter
    return false
  }
}
