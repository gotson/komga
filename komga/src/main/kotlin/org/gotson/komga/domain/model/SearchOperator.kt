package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Duration
import java.time.ZonedDateTime

class SearchOperator {
  @Schema(
    name = "SearchOperatorEquality",
    discriminatorProperty = "operator",
    oneOf = [ Is::class, IsNot::class],
    discriminatorMapping = [
      DiscriminatorMapping("is", Is::class),
      DiscriminatorMapping("isNot", IsNot::class),
    ],
  )
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Equality<T>

  @Schema(
    name = "SearchOperatorEqualityNullable",
    discriminatorProperty = "operator",
    oneOf = [ Is::class, IsNot::class, IsNullT::class, IsNotNullT::class],
    discriminatorMapping = [
      DiscriminatorMapping("is", Is::class),
      DiscriminatorMapping("isNot", IsNot::class),
      DiscriminatorMapping("isNull", IsNullT::class),
      DiscriminatorMapping("isNotNull", IsNotNullT::class),
    ],
  )
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface EqualityNullable<T>

  @Schema(
    name = "SearchOperatorString",
    discriminatorProperty = "operator",
    oneOf = [BeginsWith::class, DoesNotBeginWith::class, Contains::class, DoesNotContain::class, EndsWith::class, DoesNotEndWith::class, Is::class, IsNot::class],
    discriminatorMapping = [
      DiscriminatorMapping("beginsWith", BeginsWith::class),
      DiscriminatorMapping("doesNotBeginWith", DoesNotBeginWith::class),
      DiscriminatorMapping("contains", Contains::class),
      DiscriminatorMapping("doesNotContain", DoesNotContain::class),
      DiscriminatorMapping("endsWith", EndsWith::class),
      DiscriminatorMapping("doesNotEndWith", DoesNotEndWith::class),
      DiscriminatorMapping("is", Is::class),
      DiscriminatorMapping("isNot", IsNot::class),
    ],
  )
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface StringOp

  @Schema(
    name = "SearchOperatorNumericT",
    discriminatorProperty = "operator",
    oneOf = [GreaterThan::class, LessThan::class, Is::class, IsNot::class],
    discriminatorMapping = [
      DiscriminatorMapping("greaterThan", GreaterThan::class),
      DiscriminatorMapping("lessThan", LessThan::class),
      DiscriminatorMapping("is", Is::class),
      DiscriminatorMapping("isNot", IsNot::class),
    ],
  )
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Numeric<T>

  @Schema(
    name = "SearchOperatorNumericNullable",
    discriminatorProperty = "operator",
    oneOf = [GreaterThan::class, LessThan::class, IsNullT::class, IsNotNullT::class, Is::class, IsNot::class],
    discriminatorMapping = [
      DiscriminatorMapping("greaterThan", GreaterThan::class),
      DiscriminatorMapping("lessThan", LessThan::class),
      DiscriminatorMapping("isNull", IsNullT::class),
      DiscriminatorMapping("isNotNull", IsNotNullT::class),
      DiscriminatorMapping("is", Is::class),
      DiscriminatorMapping("isNot", IsNot::class),
    ],
  )
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface NumericNullable<T>

  @Schema(
    name = "SearchOperatorDate",
    discriminatorProperty = "operator",
    oneOf = [Before::class, After::class, IsInTheLast::class, IsNotInTheLast::class, IsNull::class, IsNotNull::class],
    discriminatorMapping = [
      DiscriminatorMapping("before", Before::class),
      DiscriminatorMapping("after", After::class),
      DiscriminatorMapping("isInTheLast", IsInTheLast::class),
      DiscriminatorMapping("isNotInTheLast", IsNotInTheLast::class),
      DiscriminatorMapping("isNull", IsNull::class),
      DiscriminatorMapping("isNotNull", IsNotNull::class),
    ],
  )
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Date

  @Schema(
    name = "SearchOperatorBoolean",
    discriminatorProperty = "operator",
    oneOf = [IsTrue::class, IsFalse::class],
    discriminatorMapping = [
      DiscriminatorMapping("isTrue", IsTrue::class),
      DiscriminatorMapping("isFalse", IsFalse::class),
    ],
  )
  @JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "operator",
  )
  sealed interface Boolean

  @Schema(name = "SearchOperatorIs")
  @JsonTypeName("is")
  data class Is<T>(
    val value: T,
  ) : Equality<T>,
    EqualityNullable<T>,
    StringOp,
    Numeric<T>,
    NumericNullable<T>

  @Schema(name = "SearchOperatorIsNot")
  @JsonTypeName("isNot")
  data class IsNot<T>(
    val value: T,
  ) : Equality<T>,
    EqualityNullable<T>,
    StringOp,
    Numeric<T>,
    NumericNullable<T>

  @Schema(name = "SearchOperatorContains")
  @JsonTypeName("contains")
  data class Contains(
    val value: String,
  ) : StringOp

  @Schema(name = "SearchOperatorDoesNotContain")
  @JsonTypeName("doesNotContain")
  data class DoesNotContain(
    val value: String,
  ) : StringOp

  @Schema(name = "SearchOperatorBeginsWith")
  @JsonTypeName("beginsWith")
  data class BeginsWith(
    val value: String,
  ) : StringOp

  @Schema(name = "SearchOperatorDoesNotBeginWith")
  @JsonTypeName("doesNotBeginWith")
  data class DoesNotBeginWith(
    val value: String,
  ) : StringOp

  @Schema(name = "SearchOperatorEndsWith")
  @JsonTypeName("endsWith")
  data class EndsWith(
    val value: String,
  ) : StringOp

  @Schema(name = "SearchOperatorDoesNotEndWith")
  @JsonTypeName("doesNotEndWith")
  data class DoesNotEndWith(
    val value: String,
  ) : StringOp

  @Schema(name = "SearchOperatorGreaterThan")
  @JsonTypeName("greaterThan")
  data class GreaterThan<T>(
    val value: T,
  ) : Numeric<T>,
    NumericNullable<T>

  @Schema(name = "SearchOperatorLessThan")
  @JsonTypeName("lessThan")
  data class LessThan<T>(
    val value: T,
  ) : Numeric<T>,
    NumericNullable<T>

  @Schema(name = "SearchOperatorBefore")
  @JsonTypeName("before")
  data class Before(
    val dateTime: ZonedDateTime,
  ) : Date

  @Schema(name = "SearchOperatorAfter")
  @JsonTypeName("after")
  data class After(
    val dateTime: ZonedDateTime,
  ) : Date

  @Schema(name = "SearchOperatorIsInTheLast")
  @JsonTypeName("isInTheLast")
  data class IsInTheLast(
    val duration: Duration,
  ) : Date

  @Schema(name = "SearchOperatorIsNotInTheLast")
  @JsonTypeName("isNotInTheLast")
  data class IsNotInTheLast(
    val duration: Duration,
  ) : Date

  @Schema(name = "SearchOperatorIsTrue")
  @JsonTypeName("isTrue")
  data object IsTrue : Boolean

  @Schema(name = "SearchOperatorIsFalse")
  @JsonTypeName("isFalse")
  data object IsFalse : Boolean

  @Schema(name = "SearchOperatorIsNull")
  @JsonTypeName("isNull")
  data object IsNull : Date

  @Schema(name = "SearchOperatorIsNotNull")
  @JsonTypeName("isNotNull")
  data object IsNotNull : Date

  @Schema(name = "SearchOperatorIsNullT")
  @JsonTypeName("isNull")
  class IsNullT<T> :
    NumericNullable<T>,
    EqualityNullable<T> {
    override fun equals(other: Any?): kotlin.Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      return true
    }

    override fun hashCode(): Int = javaClass.hashCode()
  }

  @Schema(name = "SearchOperatorIsNotNullT")
  @JsonTypeName("isNotNull")
  class IsNotNullT<T> :
    NumericNullable<T>,
    EqualityNullable<T> {
    override fun equals(other: Any?): kotlin.Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      return true
    }

    override fun hashCode(): Int = javaClass.hashCode()
  }
}
