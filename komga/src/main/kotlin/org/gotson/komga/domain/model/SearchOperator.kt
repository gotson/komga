package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.Duration
import java.time.ZonedDateTime

class SearchOperator {
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Equality<T>

  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface StringOp

  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Numeric<T>

  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface NumericNullable<T>

  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Date

  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Boolean

  @JsonTypeName("is")
  data class Is<T>(
    val value: T,
  ) : Equality<T>,
    StringOp,
    Numeric<T>,
    NumericNullable<T>

  @JsonTypeName("isNot")
  data class IsNot<T>(
    val value: T,
  ) : Equality<T>,
    StringOp,
    Numeric<T>,
    NumericNullable<T>

  @JsonTypeName("contains")
  data class Contains(
    val value: String,
  ) : StringOp

  @JsonTypeName("doesNotContain")
  data class DoesNotContain(
    val value: String,
  ) : StringOp

  @JsonTypeName("beginsWith")
  data class BeginsWith(
    val value: String,
  ) : StringOp

  @JsonTypeName("doesNotBeginWith")
  data class DoesNotBeginWith(
    val value: String,
  ) : StringOp

  @JsonTypeName("endsWith")
  data class EndsWith(
    val value: String,
  ) : StringOp

  @JsonTypeName("doesNotEndWith")
  data class DoesNotEndWith(
    val value: String,
  ) : StringOp

  @JsonTypeName("greaterThan")
  data class GreaterThan<T>(
    val value: T,
  ) : Numeric<T>,
    NumericNullable<T>

  @JsonTypeName("lessThan")
  data class LessThan<T>(
    val value: T,
  ) : Numeric<T>,
    NumericNullable<T>

  @JsonTypeName("before")
  data class Before(
    val dateTime: ZonedDateTime,
  ) : Date

  @JsonTypeName("after")
  data class After(
    val dateTime: ZonedDateTime,
  ) : Date

  @JsonTypeName("isInTheLast")
  data class IsInTheLast(
    val duration: Duration,
  ) : Date

  @JsonTypeName("isNotInTheLast")
  data class IsNotInTheLast(
    val duration: Duration,
  ) : Date

  @JsonTypeName("isTrue")
  data object IsTrue : Boolean

  @JsonTypeName("isFalse")
  data object IsFalse : Boolean

  @JsonTypeName("isNull")
  data object IsNull : Date

  @JsonTypeName("isNotNull")
  data object IsNotNull : Date

  @JsonTypeName("isNull")
  class IsNullT<T> : NumericNullable<T> {
    override fun equals(other: Any?): kotlin.Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      return true
    }

    override fun hashCode(): Int = javaClass.hashCode()
  }

  @JsonTypeName("isNotNull")
  class IsNotNullT<T> : NumericNullable<T> {
    override fun equals(other: Any?): kotlin.Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      return true
    }

    override fun hashCode(): Int = javaClass.hashCode()
  }
}
