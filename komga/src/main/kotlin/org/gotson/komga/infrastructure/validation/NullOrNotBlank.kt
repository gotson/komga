package org.gotson.komga.infrastructure.validation

import org.hibernate.validator.constraints.CompositionType
import org.hibernate.validator.constraints.ConstraintComposition
import javax.validation.Constraint
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Null
import kotlin.reflect.KClass

@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = [])
@Null
@NotBlank
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NullOrNotBlank(
  val message: String = "Must be null or not blank",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)
