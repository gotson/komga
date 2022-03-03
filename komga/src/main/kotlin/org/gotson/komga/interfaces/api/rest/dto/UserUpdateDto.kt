package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.AllowExclude
import javax.validation.Valid
import javax.validation.constraints.PositiveOrZero
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
  val restriction: AllowExclude,
)

data class SharedLibrariesUpdateDto(
  val all: Boolean,
  val libraryIds: Set<String>,
)
