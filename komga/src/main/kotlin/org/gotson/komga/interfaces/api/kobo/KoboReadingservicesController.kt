package org.gotson.komga.interfaces.api.kobo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gotson.komga.infrastructure.kobo.KoboProxy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/kobo-readingservices/{authToken}/"], produces = ["application/json; charset=utf-8"])
class KoboReadingservicesController(
  @Qualifier("koboReadingservicesProxy") private val koboProxy: KoboProxy,
  private val mapper: ObjectMapper,
) {

  @RequestMapping(
    value = ["{*path}"],
    method = [RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PATCH],
  )
  fun catchAll(
    @RequestBody body: Any?,
  ): ResponseEntity<JsonNode> =
    if (koboProxy.isEnabled())
      koboProxy.proxyCurrentRequest(body)
    else
      ResponseEntity.ok().body(mapper.createObjectNode())
}
