package org.gotson.komga.interfaces.api.rest

import jakarta.servlet.http.HttpServletRequest
import org.gotson.komga.infrastructure.security.RealIpResolver
import org.gotson.komga.infrastructure.security.getIpInfo
import org.gotson.komga.infrastructure.security.getRealIpAddress
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for IP address information.
 * Useful for debugging reverse proxy configurations.
 */
@RestController
@RequestMapping("api/v1/ip", produces = [MediaType.APPLICATION_JSON_VALUE])
class IpInfoController(
  private val realIpResolver: RealIpResolver,
) {
  /**
   * Get information about the client's IP address.
   * Shows both the real IP (from reverse proxy headers) and the remote address.
   */
  @GetMapping("info")
  fun getIpInfo(request: HttpServletRequest): Map<String, Any> {
    val ipInfo = request.getIpInfo()
    val realIp = request.getRealIpAddress()

    return mapOf(
      "clientIp" to realIp,
      "remoteAddress" to request.remoteAddr,
      "isBehindProxy" to (ipInfo?.isBehindProxy ?: false),
      "isPrivateIp" to (ipInfo?.isPrivateIp ?: false),
      "headers" to mapOf(
        "X-Forwarded-For" to (request.getHeader("X-Forwarded-For") ?: ""),
        "X-Real-IP" to (request.getHeader("X-Real-IP") ?: ""),
        "X-Forwarded-Proto" to (request.getHeader("X-Forwarded-Proto") ?: ""),
        "X-Forwarded-Host" to (request.getHeader("X-Forwarded-Host") ?: ""),
      ).filterValues { it.isNotEmpty() },
    )
  }

  /**
   * Get all headers from the request (for debugging).
   * This can help diagnose reverse proxy configuration issues.
   */
  @GetMapping("headers")
  fun getAllHeaders(request: HttpServletRequest): Map<String, List<String>> {
    val headers = mutableMapOf<String, MutableList<String>>()

    request.headerNames.asSequence().forEach { headerName ->
      val values = request.getHeaders(headerName).asSequence().toList()
      headers[headerName] = values.toMutableList()
    }

    return headers
  }

  /**
   * Simple endpoint that returns just the client IP address as plain text.
   * Useful for quick checks.
   */
  @GetMapping("", produces = [MediaType.TEXT_PLAIN_VALUE])
  fun getClientIp(request: HttpServletRequest): String {
    return request.getRealIpAddress()
  }
}
