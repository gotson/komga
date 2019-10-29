package org.gotson.komga.infrastructure.web

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest


@Component
class SPAErrorViewResolver : ErrorViewResolver {
  override fun resolveErrorView(request: HttpServletRequest, status: HttpStatus, model: MutableMap<String, Any>): ModelAndView? =
      when {
        request.requestURL.toString() == "/error" -> null
        status == HttpStatus.NOT_FOUND -> ModelAndView("/", HttpStatus.TEMPORARY_REDIRECT)
        else -> ModelAndView("/error", status)
      }
}
