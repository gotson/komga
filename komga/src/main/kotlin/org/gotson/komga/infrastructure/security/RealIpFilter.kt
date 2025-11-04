package org.gotson.komga.infrastructure.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

/**
 * Filter to extract and store the real client IP address from reverse proxy headers.
 * This filter runs early in the filter chain to ensure IP information is available
 * for security, logging, and analytics purposes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class RealIpFilter(
  private val realIpResolver: RealIpResolver,
) : OncePerRequestFilter() {
  companion object {
    const val REAL_IP_ATTRIBUTE = "komga.realIp"
    const val IP_INFO_ATTRIBUTE = "komga.ipInfo"
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    // Resolve real IP and store in request attributes
    val ipInfo = realIpResolver.getRealIpWithProxyInfo(request)
    request.setAttribute(REAL_IP_ATTRIBUTE, ipInfo.ipAddress)
    request.setAttribute(IP_INFO_ATTRIBUTE, ipInfo)

    if (ipInfo.isBehindProxy) {
      logger.debug {
        "Request from ${ipInfo.ipAddress} (proxied from ${ipInfo.remoteAddress}) to ${request.requestURI}"
      }
    }

    filterChain.doFilter(request, response)
  }
}

/**
 * Extension function to get the real IP address from the request.
 */
fun HttpServletRequest.getRealIpAddress(): String {
  return this.getAttribute(RealIpFilter.REAL_IP_ATTRIBUTE) as? String ?: this.remoteAddr
}

/**
 * Extension function to get detailed IP information from the request.
 */
fun HttpServletRequest.getIpInfo(): RealIpResolver.IpInfo? {
  return this.getAttribute(RealIpFilter.IP_INFO_ATTRIBUTE) as? RealIpResolver.IpInfo
}
