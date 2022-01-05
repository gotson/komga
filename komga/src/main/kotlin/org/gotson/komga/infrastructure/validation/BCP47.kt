package org.gotson.komga.infrastructure.validation

import com.ibm.icu.util.ULocale
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
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

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) return false
    return BCP47TagValidator.isValid(value)
  }
}

object BCP47TagValidator {
  val languages by lazy { ULocale.getISOLanguages().toSet() }

  fun isValid(value: String): Boolean {
    return ULocale.forLanguageTag(value).let {
      it.language.isNotBlank() && languages.contains(it.language)
    }
  }
}
