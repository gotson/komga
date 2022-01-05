package org.gotson.komga.infrastructure.web

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DelimitedPair(
  val parameterName: String,
)
