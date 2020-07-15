package org.gotson.komga.interfaces.rest

import mu.KotlinLogging
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.domain.model.ROLE_FILE_DOWNLOAD
import org.gotson.komga.domain.model.ROLE_PAGE_STREAMING
import org.gotson.komga.domain.model.UserEmailAlreadyExistsException
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.rest.dto.PasswordUpdateDto
import org.gotson.komga.interfaces.rest.dto.RolesUpdateDto
import org.gotson.komga.interfaces.rest.dto.SharedLibrariesUpdateDto
import org.gotson.komga.interfaces.rest.dto.UserCreationDto
import org.gotson.komga.interfaces.rest.dto.UserDto
import org.gotson.komga.interfaces.rest.dto.UserWithSharedLibrariesDto
import org.gotson.komga.interfaces.rest.dto.toDto
import org.gotson.komga.interfaces.rest.dto.toWithSharedLibrariesDto
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
  private val userLifecycle: KomgaUserLifecycle,
  private val userRepository: KomgaUserRepository,
  private val libraryRepository: LibraryRepository,
  env: Environment
) {

  private val demo = env.activeProfiles.contains("demo")

  @GetMapping("me")
  fun getMe(@AuthenticationPrincipal principal: KomgaPrincipal): UserDto =
    principal.user.toDto()

  @PatchMapping("me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updatePassword(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Valid @RequestBody newPasswordDto: PasswordUpdateDto
  ) {
    if (demo) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    userLifecycle.updatePassword(principal, newPasswordDto.password, false)
  }

  @GetMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun getAll(): List<UserWithSharedLibrariesDto> =
    userRepository.findAll().map { it.toWithSharedLibrariesDto() }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun addOne(@Valid @RequestBody newUser: UserCreationDto): UserDto =
    try {
      userLifecycle.createUser(newUser.toDomain()).toDto()
    } catch (e: UserEmailAlreadyExistsException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with this email already exists")
    }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  fun delete(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) {
    userRepository.findByIdOrNull(id)?.let {
      userLifecycle.deleteUser(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  fun updateUserRoles(
    @PathVariable id: String,
    @Valid @RequestBody patch: RolesUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal
  ) {
    userRepository.findByIdOrNull(id)?.let { user ->
      val updatedUser = user.copy(
        roleAdmin = patch.roles.contains(ROLE_ADMIN),
        roleFileDownload = patch.roles.contains(ROLE_FILE_DOWNLOAD),
        rolePageStreaming = patch.roles.contains(ROLE_PAGE_STREAMING)
      )
      userRepository.update(updatedUser)
      logger.info { "Updated user roles: $updatedUser" }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("{id}/shared-libraries")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  fun updateSharesLibraries(
    @PathVariable id: String,
    @Valid @RequestBody sharedLibrariesUpdateDto: SharedLibrariesUpdateDto
  ) {
    userRepository.findByIdOrNull(id)?.let { user ->
      val updatedUser = user.copy(
        sharedAllLibraries = sharedLibrariesUpdateDto.all,
        sharedLibrariesIds = if (sharedLibrariesUpdateDto.all) emptySet()
        else libraryRepository.findAllById(sharedLibrariesUpdateDto.libraryIds)
          .map { it.id }
          .toSet()
      )
      userRepository.update(updatedUser)
      logger.info { "Updated user shared libraries: $updatedUser" }
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}

