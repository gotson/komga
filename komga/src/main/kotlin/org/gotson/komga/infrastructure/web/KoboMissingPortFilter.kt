package org.gotson.komga.infrastructure.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class KoboMissingPortFilter(
  private val koboPortSupplier: () -> Int?,
) : OncePerRequestFilter() {
  companion object {
    private val FORWARDED_HEADER_NAMES =
      setOf(
        "Forwarded",
        "X-Forwarded-Host",
        "X-Forwarded-Port",
        "X-Forwarded-Proto",
        "X-Forwarded-Prefix",
        "X-Forwarded-Ssl",
        "X-Forwarded-For",
      )
  }

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    // ignore this filter if forwarded headers are present
    FORWARDED_HEADER_NAMES.forEach { if (request.getHeader(it) != null) return true }
    return false
  }

  override fun shouldNotFilterAsyncDispatch(): Boolean = false

  override fun shouldNotFilterErrorDispatch(): Boolean = false

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    val wrappedRequest =
      try {
        KoboMissingPortRequest(request, koboPortSupplier)
      } catch (ex: Throwable) {
        if (logger.isDebugEnabled) logger.debug("Failed to apply missing port to " + formatRequest(request), ex)
        response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        return
      }
    filterChain.doFilter(wrappedRequest, response)
  }

  override fun doFilterNestedErrorDispatch(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    doFilterInternal(request, response, filterChain)
  }

  private fun formatRequest(request: HttpServletRequest) = "HTTP ${request.method} \"${request.requestURI}\""

  private class KoboMissingPortRequest(
    request: HttpServletRequest,
    val port: () -> Int?,
  ) : HttpServletRequestWrapper(request) {
    override fun getServerPort() = port() ?: request.serverPort
  }
}
