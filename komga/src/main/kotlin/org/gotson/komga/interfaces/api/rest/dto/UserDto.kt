package org.gotson.komga.interfaces.api.rest.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_KOBO_SYNC
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.infrastructure.security.KomgaPrincipal

data class UserDto(
  val id: String,
  val email: String,
  val roles: Set<String>,
  val sharedAllLibraries: Boolean,
  val sharedLibrariesIds: Set<String>,
  val labelsAllow: Set<String>,
  val labelsExclude: Set<String>,
  val ageRestriction: AgeRestrictionDto?,
)

data class AgeRestrictionDto(
  val age: Int,
  val restriction: AllowExclude,
)

fun AgeRestriction.toDto() = AgeRestrictionDto(age, restriction)

fun KomgaUser.toDto() =
  UserDto(
    id = id,
    email = email,
    roles = roles,
    sharedAllLibraries = sharedAllLibraries,
    sharedLibrariesIds = sharedLibrariesIds,
    labelsAllow = restrictions.labelsAllow,
    labelsExclude = restrictions.labelsExclude,
    ageRestriction = restrictions.ageRestriction?.toDto(),
  )

fun KomgaPrincipal.toDto() = user.toDto()

data class UserCreationDto(
  @get:Email(regexp = ".+@.+\\..+") val email: String,
  @get:NotBlank val password: String,
  val roles: List<String> = emptyList(),
) {
  fun toDomain(): KomgaUser =
    KomgaUser(
      email,
      password,
      roleAdmin = roles.contains(ROLE_ADMIN),
      roleFileDownload = roles.contains(ROLE_FILE_DOWNLOAD),
      rolePageStreaming = roles.contains(ROLE_PAGE_STREAMING),
      roleKoboSync = roles.contains(ROLE_KOBO_SYNC),
    )
}

data class PasswordUpdateDto(
  @get:NotBlank val password: String,
)
