package org.gotson.komga.interfaces.rest

import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.infrastructure.security.KomgaUserDetailsLifecycle
import org.gotson.komga.interfaces.rest.dto.toDto
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.User
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Profile("claim")
@RestController
@RequestMapping("api/v1/claim", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
class ClaimController(
  private val userDetailsLifecycle: KomgaUserDetailsLifecycle
) {
  @PostMapping
  fun claimAdmin(
    @Email @RequestHeader("X-Komga-Email") email: String,
    @NotBlank @RequestHeader("X-Komga-Password") password: String
  ): UserDto {
    if (userDetailsLifecycle.countUsers() > 0)
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This server has already been claimed")

    return (userDetailsLifecycle.createUser(
      User.withUsername(email)
        .password(password)
        .roles(UserRoles.ADMIN.name)
        .build()) as KomgaPrincipal
      ).toDto()
  }
}
