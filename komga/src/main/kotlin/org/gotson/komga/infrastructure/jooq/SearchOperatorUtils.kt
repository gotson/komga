package org.gotson.komga.infrastructure.jooq

import org.gotson.komga.domain.model.SearchOperator
import org.gotson.komga.language.stripAccents
import org.jooq.Field
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun SearchOperator.Equality<String>.toCondition(
  field: Field<String>,
  ignoreCase: Boolean = false,
) = when (this) {
  is SearchOperator.Is -> if (ignoreCase) field.unicode1().equal(this.value) else field.equal(this.value)
  is SearchOperator.IsNot -> if (ignoreCase) field.unicode1().notEqual(this.value) else field.notEqual(this.value)
}

fun <T> SearchOperator.Equality<T>.toCondition(field: Field<T>) =
  when (this) {
    is SearchOperator.Is -> field.eq(this.value)
    is SearchOperator.IsNot -> field.ne(this.value)
  }

fun <T> SearchOperator.Equality<T>.toCondition(
  field: Field<String>,
  converter: (T) -> String,
) = when (this) {
  is SearchOperator.Is -> field.eq(converter(this.value))
  is SearchOperator.IsNot -> field.ne(converter(this.value))
}

fun SearchOperator.StringOp.toCondition(field: Field<String>) =
  when (this) {
    is SearchOperator.BeginsWith -> field.udfStripAccents().startsWithIgnoreCase(value.stripAccents())
    is SearchOperator.DoesNotBeginWith -> field.udfStripAccents().startsWithIgnoreCase(value.stripAccents()).not()
    is SearchOperator.EndsWith -> field.udfStripAccents().endsWithIgnoreCase(value.stripAccents())
    is SearchOperator.DoesNotEndWith -> field.udfStripAccents().endsWithIgnoreCase(value.stripAccents()).not()
    is SearchOperator.Contains -> field.udfStripAccents().containsIgnoreCase(value.stripAccents())
    is SearchOperator.DoesNotContain -> field.udfStripAccents().notContainsIgnoreCase(value.stripAccents())
    is SearchOperator.Is<*> -> field.unicode1().equal(value as String)
    is SearchOperator.IsNot<*> -> field.unicode1().notEqual(value as String)
  }

fun SearchOperator.Date.toCondition(field: Field<LocalDate>) =
  when (this) {
    is SearchOperator.After -> field.gt(dateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDate())
    is SearchOperator.Before -> field.lt(dateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDate())
    is SearchOperator.IsInTheLast -> field.gt(LocalDate.now(ZoneOffset.UTC).minus(duration.toDays(), ChronoUnit.DAYS))
    is SearchOperator.IsNotInTheLast -> field.lt(LocalDate.now(ZoneOffset.UTC).minus(duration.toDays(), ChronoUnit.DAYS))
    SearchOperator.IsNull -> field.isNull
    SearchOperator.IsNotNull -> field.isNotNull
  }

fun SearchOperator.NumericNullable<Int>.toCondition(field: Field<Int>) =
  when (this) {
    is SearchOperator.Is<*> -> field.eq(value as Int)
    is SearchOperator.IsNot<*> -> field.ne(value as Int).or(field.isNull)
    is SearchOperator.GreaterThan -> field.gt(value)
    is SearchOperator.LessThan -> field.lt(value)
    is SearchOperator.IsNullT -> field.isNull
    is SearchOperator.IsNotNullT -> field.isNotNull
  }

fun SearchOperator.Numeric<Float>.toCondition(field: Field<Float>) =
  when (this) {
    is SearchOperator.Is<*> -> field.eq(value as Float)
    is SearchOperator.IsNot<*> -> field.ne(value as Float)
    is SearchOperator.GreaterThan -> field.gt(value)
    is SearchOperator.LessThan -> field.lt(value)
  }

fun SearchOperator.Boolean.toCondition(field: Field<Boolean>) =
  when (this) {
    SearchOperator.IsTrue -> field.isTrue
    SearchOperator.IsFalse -> field.isFalse
  }
