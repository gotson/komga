package org.gotson.komga.interfaces.api.kobo

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.gotson.komga.interfaces.api.kobo.dto.AuthDto
import org.gotson.komga.interfaces.api.kobo.dto.ResourcesDto
import org.gotson.komga.interfaces.api.kobo.dto.TestsDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

private val logger = KotlinLogging.logger {}

const val X_KOBO_USERKEY = "X-Kobo-userkey"

@RestController
@RequestMapping(value = ["/kobo/{deviceToken}"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KoboController(
  private val koboProxy: KoboProxy,
) {
  //  @GetMapping("/v1/initialization")
  fun initialization(): ResponseEntity<ResourcesDto> {
    val resources =
      try {
        koboProxy.proxyCurrentRequest().body?.get("Resources")
      } catch (e: Exception) {
        logger.warn { "Failed to get response from Kobo's init endpoint, fallback to noproxy" }
        null
      } ?: koboProxy.nativeKoboResources

    return ResponseEntity.ok()
      .header("x-kobo-apitoken", "e30=")
      .body(ResourcesDto(resources))
  }

//  @PostMapping("v1/auth/device")
  fun authDevice(
    @RequestBody body: JsonNode,
  ): AuthDto {
    val response =
      AuthDto(
        accessToken = RandomStringUtils.randomAlphanumeric(24),
        refreshToken = RandomStringUtils.randomAlphanumeric(24),
        trackingId = UUID.randomUUID().toString(),
        userKey = body.get("UserKey").asText(),
      )

    logger.debug { "Auth response: $response" }
    return response
  }

//  @RequestMapping(value = ["/v1/analytics/gettests"], method = [RequestMethod.GET, RequestMethod.POST])
  fun analyticsGetTests(
    @RequestHeader(name = X_KOBO_USERKEY, required = false) userKey: String?,
  ) = TestsDto(
    result = "Success",
    testKey = userKey ?: "",
  )

  @GetMapping("v1/library/sync")
  fun syncLibrary(): ResponseEntity<JsonNode> {
    return koboProxy.proxyCurrentRequest(includeSyncToken = true)
  }

  @RequestMapping(
    value = ["{*path}"],
    method = [RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PATCH],
  )
  fun catchAll(
    @RequestBody body: Any?,
  ): ResponseEntity<JsonNode> {
    return koboProxy.proxyCurrentRequest(body)
  }
}
