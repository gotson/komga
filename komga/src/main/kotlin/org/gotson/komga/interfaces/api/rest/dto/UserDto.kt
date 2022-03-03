@file:Suppress("DEPRECATION")

package org.gotson.komga.interfaces.api.rest.dto

import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.AllowExclude
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Deprecated("Deprecated since 0.153.0. Use UserDtoV2 instead")
data class UserDto(
  val id: String,
  val email: String,
  val roles: List<String>,
)

@Deprecated("Deprecated since 0.153.0. Use toDtoV2() instead")
fun KomgaUser.toDto() =
  UserDto(
    id = id,
    email = email,
    roles = roles.toList(),
  )

@Deprecated("Deprecated since 0.153.0. Use toDtoV2() instead")
fun KomgaPrincipal.toDto() = user.toDto()

data class UserDtoV2(
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

fun KomgaUser.toDtoV2() =
  UserDtoV2(
    id = id,
    email = email,
    roles = roles,
    sharedAllLibraries = sharedAllLibraries,
    sharedLibrariesIds = sharedLibrariesIds,
    labelsAllow = restrictions.labelsAllow,
    labelsExclude = restrictions.labelsExclude,
    ageRestriction = restrictions.ageRestriction?.toDto(),
  )

fun KomgaPrincipal.toDtoV2() = user.toDtoV2()

@Deprecated("Deprecated since 0.153.0. Use UserDtoV2 instead")
data class UserWithSharedLibrariesDto(
  val id: String,
  val email: String,
  val roles: List<String>,
  val sharedAllLibraries: Boolean,
  val sharedLibraries: List<SharedLibraryDto>,
)

@Deprecated("Deprecated since 0.153.0. Use UserDtoV2 instead")
data class SharedLibraryDto(
  val id: String,
)

@Deprecated("Deprecated since 0.153.0. Use toDtoV2() instead")
fun KomgaUser.toWithSharedLibrariesDto() =
  UserWithSharedLibrariesDto(
    id = id,
    email = email,
    roles = roles.toList(),
    sharedAllLibraries = sharedAllLibraries,
    sharedLibraries = sharedLibrariesIds.map { SharedLibraryDto(it) },
  )

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
    )
}

data class PasswordUpdateDto(
  @get:NotBlank val password: String,
)

@Deprecated("Deprecated since 0.153.0")
data class RolesUpdateDto(
  val roles: List<String>,
)
