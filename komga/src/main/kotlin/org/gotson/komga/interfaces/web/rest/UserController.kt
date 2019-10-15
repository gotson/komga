package org.gotson.komga.interfaces.web.rest

import mu.KotlinLogging
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.security.KomgaUserDetailsLifecycle
import org.gotson.komga.infrastructure.security.UserEmailAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
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
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v1/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    private val userDetailsLifecycle: KomgaUserDetailsLifecycle,
    private val userRepository: KomgaUserRepository
) {

  @GetMapping("me")
  fun getMe(@AuthenticationPrincipal principal: KomgaPrincipal): UserDto =
      principal.user.toDto()

  @PatchMapping("me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updatePassword(
      @AuthenticationPrincipal principal: KomgaPrincipal,
      @Valid @RequestBody newPasswordDto: PasswordUpdateDto
  ) {
    userDetailsLifecycle.updatePassword(principal, newPasswordDto.password)
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  fun getAll(): List<UserDto> =
      userRepository.findAll().map { it.toDto() }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  fun addOne(@Valid @RequestBody newUser: UserCreationDto): UserDto =
      try {
        (userDetailsLifecycle.createUser(newUser.toUserDetails()) as KomgaPrincipal).toDto()
      } catch (e: UserEmailAlreadyExistsException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with this email already exists")
      }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  fun delete(@PathVariable id: Long) {
    if (userRepository.existsById(id))
      userRepository.deleteById(id)
    else
      throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}

data class UserDto(
    val id: Long,
    val email: String,
    val roles: List<String>
)

fun KomgaUser.toDto() =
    UserDto(
        id = id,
        email = email,
        roles = roles.map { it.name }
    )

fun KomgaPrincipal.toDto() = user.toDto()

data class UserCreationDto(
    @get:Email val email: String,
    @get:NotBlank val password: String,
    val roles: List<String> = emptyList()
) {
  fun toUserDetails(): UserDetails =
      User.withUsername(email)
          .password(password)
          .roles(*roles.toTypedArray())
          .build()
}

data class PasswordUpdateDto(
    @get:NotBlank val password: String
)
