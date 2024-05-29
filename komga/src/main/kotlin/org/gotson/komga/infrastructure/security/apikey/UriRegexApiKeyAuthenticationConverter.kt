package org.gotson.komga.infrastructure.security.apikey

import jakarta.servlet.http.HttpServletRequest
import org.gotson.komga.infrastructure.security.TokenEncoder
import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationConverter

/**
 * A strategy that uses a regex to retrieve the API key from the
 * request URI, and convert it to an [ApiKeyAuthenticationToken]
 *
 * @property tokenRegex the regex used to extract the API key
 * @property tokenEncoder the encoder to use to encode the API key in the [Authentication] object
 * @property authenticationDetailsSource the [AuthenticationDetailsSource] to enrich the [Authentication] details
 */
class UriRegexApiKeyAuthenticationConverter(
  private val tokenRegex: Regex,
  private val tokenEncoder: TokenEncoder,
  private val authenticationDetailsSource: AuthenticationDetailsSource<HttpServletRequest, *>,
) : AuthenticationConverter {
  override fun convert(request: HttpServletRequest): Authentication? =
    request.requestURI?.let {
      tokenRegex.find(it)?.groupValues?.lastOrNull()
    }?.let {
      val (maskedToken, hashedToken) = it.take(6) + "*".repeat(6) to tokenEncoder.encode(it)
      ApiKeyAuthenticationToken.unauthenticated(maskedToken, hashedToken)
        .apply { details = authenticationDetailsSource.buildDetails(request) }
    }
}
