package org.gotson.komga.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Null
import org.hibernate.validator.constraints.CompositionType
import org.hibernate.validator.constraints.ConstraintComposition
import kotlin.reflect.KClass

@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = [])
@Null
@NotEmpty
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NullOrNotEmpty(
  val message: String = "Must be null or not empty",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)
