package org.gotson.komga.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.gotson.komga.interfaces.api.OpdsGenerator
import org.gotson.komga.interfaces.api.dto.MEDIATYPE_OPDS_AUTHENTICATION_JSON_VALUE
import org.gotson.komga.interfaces.api.dto.OpdsLinkRel
import org.gotson.komga.interfaces.api.opds.v2.ROUTE_AUTH
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

private const val DEFAULT_REALM: String = "Realm"

@Component
class OpdsAuthenticationEntryPoint(
  private val opdsGenerator: OpdsGenerator,
  private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
  override fun commence(
    request: HttpServletRequest,
    response: HttpServletResponse,
    authException: AuthenticationException,
  ) {
    with(response) {
      contentType = MEDIATYPE_OPDS_AUTHENTICATION_JSON_VALUE
      characterEncoding = Charsets.UTF_8.name()
      status = HttpStatus.UNAUTHORIZED.value()
      setHeader("WWW-Authenticate", """Basic realm="$DEFAULT_REALM"""")
      setHeader(
        HttpHeaders.LINK,
        """<${ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("opds", "v2").path(ROUTE_AUTH).toUriString()}>; rel="${OpdsLinkRel.AUTH}"; type="$MEDIATYPE_OPDS_AUTHENTICATION_JSON_VALUE"""",
      )
      with(writer) {
        write(objectMapper.writeValueAsString(opdsGenerator.generateOpdsAuthDocument()))
        flush()
      }
    }
  }
}
