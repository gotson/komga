package org.gotson.komga.infrastructure.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.gotson.komga.language.toEnumeration
import java.util.Enumeration

class BracketParamsRequestWrapper(
  request: HttpServletRequest,
) : HttpServletRequestWrapper(request) {
  override fun getParameter(name: String): String? {
    val nameWithoutSuffix = name.removeSuffix("[]")
    val values = listOfNotNull(super.getParameter(nameWithoutSuffix), super.getParameter("$nameWithoutSuffix[]"))
    return if (values.isEmpty())
      null
    else
      values.joinToString(",")
  }

  override fun getParameterValues(name: String): Array<String>? {
    val nameWithoutSuffix = name.removeSuffix("[]")
    val regular = super.getParameterValues(nameWithoutSuffix)
    val suffix = super.getParameterValues("$nameWithoutSuffix[]")
    val values = listOfNotNull(regular, suffix)
    return if (values.isEmpty())
      null
    else
      values.reduce { acc, strings -> acc + strings }
  }

  override fun getParameterNames(): Enumeration<String> =
    super
      .getParameterNames()
      .toList()
      .map { it.removeSuffix("[]") }
      .distinct()
      .toEnumeration()

  override fun getParameterMap(): MutableMap<String, Array<String>> =
    super
      .getParameterMap()
      .asSequence()
      .groupBy({ it.key.removeSuffix("[]") }, { it.value })
      .mapValues { it.value.reduce { acc, strings -> acc + strings } }
      .toMutableMap()
}
