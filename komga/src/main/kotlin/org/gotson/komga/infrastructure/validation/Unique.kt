package org.gotson.komga.infrastructure.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.reflect.KClass


@Constraint(validatedBy = [UniqueValidator::class])
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Unique(
  val message: String = "Must contain unique values",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = []
)

class UniqueValidator : ConstraintValidator<Unique, List<*>> {
  override fun isValid(value: List<*>?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) return true
    return value.distinct().size == value.size
  }
}
