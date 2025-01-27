package org.gotson.komga.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.gotson.komga.domain.model.BCP47TagValidator
import kotlin.reflect.KClass

@Constraint(validatedBy = [BCP47Validator::class])
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BCP47(
  val message: String = "Must be a valid BCP 47 language tag",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class BCP47Validator : ConstraintValidator<BCP47, String> {
  override fun isValid(
    value: String?,
    context: ConstraintValidatorContext?,
  ): Boolean {
    if (value == null) return false
    return BCP47TagValidator.isValid(value)
  }
}
