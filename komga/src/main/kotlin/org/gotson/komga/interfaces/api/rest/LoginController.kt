package org.gotson.komga.interfaces.api.rest

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.session.web.http.CookieSerializer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class LoginController(
  private val cookieSerializer: CookieSerializer,
) {
  @GetMapping("api/v1/login/set-cookie")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun headerToCookie(
    request: HttpServletRequest,
    response: HttpServletResponse,
    session: HttpSession,
  ) {
    cookieSerializer.writeCookieValue(CookieSerializer.CookieValue(request, response, session.id))
  }
}
