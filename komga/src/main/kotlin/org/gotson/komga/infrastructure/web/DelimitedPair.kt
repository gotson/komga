package org.gotson.komga.infrastructure.web

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Deprecated("was used only for search_regex which is deprecated")
annotation class DelimitedPair(
  val parameterName: String,
)
