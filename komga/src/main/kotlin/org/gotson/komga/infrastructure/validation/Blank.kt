package org.gotson.komga.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [BlankValidator::class])
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Blank(
  val message: String = "Must be blank",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class BlankValidator : ConstraintValidator<Blank, String> {
  override fun isValid(
    value: String?,
    context: ConstraintValidatorContext?,
  ): Boolean {
    if (value == null) return false
    return value.isBlank()
  }
}
