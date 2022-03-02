package org.gotson.komga.interfaces.api.rest.dto

import kotlin.properties.Delegates

class UserUpdateDto {
  private val isSet = mutableMapOf<String, Boolean>()
  fun isSet(prop: String) = isSet.getOrDefault(prop, false)

//  @get:NullOrNotBlank
//  val title: String? = null

//  @get:PositiveOrZero
//  var ageRating: Int?
//    by Delegates.observable(null) { prop, _, _ ->
//      isSet[prop.name] = true
//    }

  var roles: Set<String>?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }

  var sharedLibraries: SharedLibrariesUpdateDto?
    by Delegates.observable(null) { prop, _, _ ->
      isSet[prop.name] = true
    }
}

data class SharedLibrariesUpdateDto(
  val all: Boolean,
  val libraryIds: Set<String>,
)
