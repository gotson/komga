package org.gotson.komga.interfaces.api.rest

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.gotson.komga.domain.model.KomgaUser
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.interfaces.api.rest.dto.UserDto
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api/v1/claim", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
class ClaimController(
  private val userDetailsLifecycle: KomgaUserLifecycle,
) {
  @GetMapping
  fun getClaimStatus() = ClaimStatus(userDetailsLifecycle.countUsers() > 0)

  @PostMapping
  fun claimAdmin(
    @Email(regexp = ".+@.+\\..+")
    @RequestHeader("X-Komga-Email")
    email: String,
    @NotBlank
    @RequestHeader("X-Komga-Password")
    password: String,
  ): UserDto {
    if (userDetailsLifecycle.countUsers() > 0)
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This server has already been claimed")

    return userDetailsLifecycle
      .createUser(
        KomgaUser(
          email = email,
          password = password,
          roles = UserRoles.entries.toSet(),
        ),
      ).toDto()
  }

  data class ClaimStatus(
    val isClaimed: Boolean,
  )
}
