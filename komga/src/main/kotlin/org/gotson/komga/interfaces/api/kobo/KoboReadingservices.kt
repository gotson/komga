package org.gotson.komga.interfaces.api.kobo

import org.gotson.komga.infrastructure.kobo.KoboProxy
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/kobo-readingservices/{authToken}/"], produces = ["application/json; charset=utf-8"])
class KoboReadingservices(
  private val koboProxy: KoboProxy
) {

}
