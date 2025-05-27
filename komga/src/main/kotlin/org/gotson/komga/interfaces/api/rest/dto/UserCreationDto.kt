package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserCreationDto(
  @get:Email(regexp = ".+@.+\\..+") val email: String,
  @get:NotBlank val password: String,
  val roles: List<String> = emptyList(),
  // new fields supported
  @get:Valid val ageRestriction: AgeRestrictionUpdateDto?,
  val labelsAllow: Set<String>?,
  val labelsExclude: Set<String>?,
  val sharedLibraries: SharedLibrariesUpdateDto?,
)
