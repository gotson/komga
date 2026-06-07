package org.gotson.komga.infrastructure.security.oauth2

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.InputStream

/**
 * Interceptor that sanitizes malformed Content-Type headers returned by some OIDC providers
 * (e.g. Cloudflare ZeroTrust) which send duplicate values like:
 * "application/json; charset=utf-8, application/json"
 *
 * Spring's MimeTypeUtils cannot parse such values, so we strip everything after the first
 * comma-separated duplicate to produce a valid single media type.
 */
class LenientContentTypeInterceptor : ClientHttpRequestInterceptor {
  override fun intercept(
    request: org.springframework.http.HttpRequest,
    body: ByteArray,
    execution: ClientHttpRequestExecution,
  ): ClientHttpResponse {
    val response = execution.execute(request, body)
    return ContentTypeSanitizingResponse(response)
  }

  private class ContentTypeSanitizingResponse(
    private val delegate: ClientHttpResponse,
  ) : ClientHttpResponse {
    private val sanitizedHeaders: HttpHeaders by lazy {
      val headers = HttpHeaders()
      delegate.headers.forEach { (name, values) ->
        if (name.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
          // Take only the first media type value if multiple are comma-separated within a single value
          val sanitized =
            values.map { value ->
              val commaIndex = value.indexOf(',')
              if (commaIndex > 0) value.substring(0, commaIndex).trim() else value
            }
          headers[name] = sanitized
        } else {
          headers[name] = values
        }
      }
      headers
    }

    override fun getHeaders(): HttpHeaders = sanitizedHeaders

    override fun getBody(): InputStream = delegate.body

    override fun getStatusCode(): HttpStatusCode = delegate.statusCode

    override fun getStatusText(): String = delegate.statusText

    override fun close() = delegate.close()
  }
}
