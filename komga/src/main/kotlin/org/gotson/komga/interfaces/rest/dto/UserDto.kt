package org.gotson.komga.interfaces.rest.dto

import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class UserDto(
  val id: String,
  val email: String,
  val roles: List<String>
)

fun KomgaUser.toDto() =
  UserDto(
    id = id,
    email = email,
    roles = roles().toList()
  )

fun KomgaPrincipal.toDto() = user.toDto()

data class UserWithSharedLibrariesDto(
  val id: String,
  val email: String,
  val roles: List<String>,
  val sharedAllLibraries: Boolean,
  val sharedLibraries: List<SharedLibraryDto>
)

data class SharedLibraryDto(
  val id: String
)

fun KomgaUser.toWithSharedLibrariesDto() =
  UserWithSharedLibrariesDto(
    id = id,
    email = email,
    roles = roles().toList(),
    sharedAllLibraries = sharedAllLibraries,
    sharedLibraries = sharedLibrariesIds.map { SharedLibraryDto(it) }
  )

data class UserCreationDto(
  @get:Email val email: String,
  @get:NotBlank val password: String,
  val roles: List<String> = emptyList()
) {
  fun toDomain(): KomgaUser =
    KomgaUser(
      email,
      password,
      roleAdmin = roles.contains(ROLE_ADMIN),
      roleFileDownload = roles.contains(ROLE_FILE_DOWNLOAD),
      rolePageStreaming = roles.contains(ROLE_PAGE_STREAMING)
    )
}

data class PasswordUpdateDto(
  @get:NotBlank val password: String
)

data class SharedLibrariesUpdateDto(
  val all: Boolean,
  val libraryIds: Set<String>
)

data class RolesUpdateDto(
  val roles: List<String>
)
