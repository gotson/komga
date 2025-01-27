package org.gotson.komga.infrastructure.web

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class DelimitedPairHandlerMethodArgumentResolver : HandlerMethodArgumentResolver {
  override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.getParameterAnnotation(DelimitedPair::class.java) != null

  override fun resolveArgument(
    parameter: MethodParameter,
    mavContainer: ModelAndViewContainer?,
    webRequest: NativeWebRequest,
    binderFactory: WebDataBinderFactory?,
  ): Pair<String, String>? {
    val paramName = parameter.getParameterAnnotation(DelimitedPair::class.java)?.parameterName ?: return null
    val param = webRequest.getParameterValues(paramName) ?: return null

    // Single empty parameter, e.g "search="
    if (param.size == 1 && param[0].isNullOrBlank()) return null

    return parseParameterIntoPairs(param.first())
  }

  private fun parseParameterIntoPairs(
    source: String,
    delimiter: String = ",",
  ): Pair<String, String>? =
    if (!source.contains(delimiter))
      null
    else
      Pair(source.substringBeforeLast(delimiter), source.substringAfterLast(delimiter))
}
