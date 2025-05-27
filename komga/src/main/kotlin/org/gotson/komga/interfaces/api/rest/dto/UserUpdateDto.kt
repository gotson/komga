package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.PositiveOrZero
import org.gotson.komga.domain.model.AllowExclude
import kotlin.properties.Delegates

class UserUpdateDto {
  private val isSet = mutableMapOf<String, Boolean>()

  fun isSet(prop: String) = isSet.getOrDefault(prop, false)

  @get:Valid
  var ageRestriction: AgeRestrictionUpdateDto?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var labelsAllow: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var labelsExclude: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var roles: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var sharedLibraries: SharedLibrariesUpdateDto?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }
}

data class AgeRestrictionUpdateDto(
  @get:PositiveOrZero
  val age: Int,
  val restriction: AllowExcludeDto,
)

data class SharedLibrariesUpdateDto(
  val all: Boolean,
  val libraryIds: Set<String>,
)

enum class AllowExcludeDto {
  ALLOW_ONLY,
  EXCLUDE,
  NONE,
  ;

  fun toDomain() =
    when (this) {
      ALLOW_ONLY -> AllowExclude.ALLOW_ONLY
      EXCLUDE -> AllowExclude.EXCLUDE
      NONE -> throw IllegalArgumentException()
    }
}
