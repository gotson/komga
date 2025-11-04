package org.gotson.komga.infrastructure.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Resolves the real IP address from HTTP requests, considering reverse proxy headers.
 * Supports X-Forwarded-For, X-Real-IP, and other common reverse proxy headers.
 */
@Component
class RealIpResolver {
  companion object {
    // Common reverse proxy headers in order of preference
    private val IP_HEADERS = listOf(
      "X-Forwarded-For",
      "X-Real-IP",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_X_FORWARDED_FOR",
      "HTTP_X_FORWARDED",
      "HTTP_X_CLUSTER_CLIENT_IP",
      "HTTP_CLIENT_IP",
      "HTTP_FORWARDED_FOR",
      "HTTP_FORWARDED",
      "HTTP_VIA",
      "REMOTE_ADDR",
    )

    // Private IP ranges (RFC 1918)
    private val PRIVATE_IP_PATTERNS = listOf(
      Regex("^127\\."),
      Regex("^10\\."),
      Regex("^172\\.(1[6-9]|2[0-9]|3[0-1])\\."),
      Regex("^192\\.168\\."),
      Regex("^169\\.254\\."),
      Regex("^::1$"),
      Regex("^fe80:"),
      Regex("^fc00:"),
    )
  }

  /**
   * Extract the real client IP address from the request.
   * Considers reverse proxy headers and falls back to remote address.
   */
  fun getRealIpAddress(request: HttpServletRequest): String {
    // Try to get IP from headers
    for (header in IP_HEADERS) {
      val headerValue = request.getHeader(header)
      if (!headerValue.isNullOrBlank() && headerValue != "unknown") {
        // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
        // We want the first (client) IP
        val ip = headerValue.split(",")[0].trim()
        if (isValidIp(ip)) {
          logger.debug { "Resolved IP from header $header: $ip" }
          return ip
        }
      }
    }

    // Fallback to remote address
    val remoteAddr = request.remoteAddr
    logger.debug { "Using remote address: $remoteAddr" }
    return remoteAddr
  }

  /**
   * Get the real IP address and determine if it's from a trusted proxy.
   */
  fun getRealIpWithProxyInfo(request: HttpServletRequest): IpInfo {
    val realIp = getRealIpAddress(request)
    val remoteAddr = request.remoteAddr
    val isBehindProxy = realIp != remoteAddr
    val isPrivateIp = isPrivateIp(realIp)

    return IpInfo(
      ipAddress = realIp,
      remoteAddress = remoteAddr,
      isBehindProxy = isBehindProxy,
      isPrivateIp = isPrivateIp,
      forwardedFor = request.getHeader("X-Forwarded-For"),
    )
  }

  /**
   * Validate that the string is a valid IP address.
   */
  private fun isValidIp(ip: String): Boolean {
    if (ip.isBlank() || ip.length > 45) return false

    // IPv4 validation
    val ipv4Pattern = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
    if (ipv4Pattern.matches(ip)) return true

    // IPv6 validation (simplified)
    val ipv6Pattern = Regex("^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$")
    if (ipv6Pattern.matches(ip)) return true

    // IPv6 with :: compression
    if (ip.contains("::") && ip.count { it == ':' } < 8) return true

    return false
  }

  /**
   * Check if the IP address is in a private range.
   */
  private fun isPrivateIp(ip: String): Boolean {
    return PRIVATE_IP_PATTERNS.any { it.containsMatchIn(ip) }
  }

  /**
   * Information about the resolved IP address.
   */
  data class IpInfo(
    val ipAddress: String,
    val remoteAddress: String,
    val isBehindProxy: Boolean,
    val isPrivateIp: Boolean,
    val forwardedFor: String?,
  )
}
