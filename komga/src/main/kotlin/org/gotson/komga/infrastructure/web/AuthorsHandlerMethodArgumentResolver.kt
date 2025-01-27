package org.gotson.komga.infrastructure.web

import org.gotson.komga.domain.model.Author
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class AuthorsHandlerMethodArgumentResolver : HandlerMethodArgumentResolver {
  override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.getParameterAnnotation(Authors::class.java) != null

  override fun resolveArgument(
    parameter: MethodParameter,
    mavContainer: ModelAndViewContainer?,
    webRequest: NativeWebRequest,
    binderFactory: WebDataBinderFactory?,
  ): Any? {
    val param = webRequest.getParameterValues("author") ?: return null

    // Single empty parameter, e.g "author="
    if (param.size == 1 && param[0].isNullOrBlank()) return null

    return parseParameterIntoAuthors(param.toList())
  }

  private fun parseParameterIntoAuthors(
    source: List<String>,
    delimiter: String = ",",
  ): List<Author> =
    source
      .filter { it.contains(delimiter) }
      .map { Author(name = it.substringBeforeLast(delimiter), role = it.substringAfterLast(delimiter)) }
}
