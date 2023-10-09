package org.gotson.komga.interfaces.mvc

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.NoHandlerFoundException

@Component
@ControllerAdvice
class ResourceNotFoundController {
  val apis = listOf("/api", "/opds", "/sse")

  @ExceptionHandler(NoHandlerFoundException::class)
  fun notFound(request: HttpServletRequest): String {
    if (apis.any { request.requestURI.startsWith(it, true) }) throw ResponseStatusException(HttpStatus.NOT_FOUND)
    return "forward:/"
  }
}
