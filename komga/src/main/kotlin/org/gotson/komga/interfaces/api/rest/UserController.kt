package org.gotson.komga.interfaces.api.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.gotson.komga.domain.model.AgeRestriction
import org.gotson.komga.domain.model.ContentRestrictions
import org.gotson.komga.domain.model.DuplicateNameException
import org.gotson.komga.domain.model.UserEmailAlreadyExistsException
import org.gotson.komga.domain.model.UserRoles
import org.gotson.komga.domain.persistence.AuthenticationActivityRepository
import org.gotson.komga.domain.persistence.KomgaUserRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.service.KomgaUserLifecycle
import org.gotson.komga.infrastructure.jooq.UnpagedSorted
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.ApiKeyDto
import org.gotson.komga.interfaces.api.rest.dto.ApiKeyRequestDto
import org.gotson.komga.interfaces.api.rest.dto.AuthenticationActivityDto
import org.gotson.komga.interfaces.api.rest.dto.PasswordUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.UserCreationDto
import org.gotson.komga.interfaces.api.rest.dto.UserDto
import org.gotson.komga.interfaces.api.rest.dto.UserUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.redacted
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.core.env.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("api/v2/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
  private val userLifecycle: KomgaUserLifecycle,
  private val userRepository: KomgaUserRepository,
  private val libraryRepository: LibraryRepository,
  private val authenticationActivityRepository: AuthenticationActivityRepository,
  env: Environment,
) {
  private val demo = env.activeProfiles.contains("demo")

  @GetMapping("me")
  fun getMe(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): UserDto = principal.toDto()

  @PatchMapping("me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateMyPassword(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Valid @RequestBody
    newPasswordDto: PasswordUpdateDto,
  ) {
    if (demo) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    userRepository.findByEmailIgnoreCaseOrNull(principal.username)?.let { user ->
      userLifecycle.updatePassword(user, newPasswordDto.password, false)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  fun getAll(): List<UserDto> = userRepository.findAll().map { it.toDto() }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  fun addOne(
    @Valid @RequestBody
    newUser: UserCreationDto,
  ): UserDto =
    try {
      userLifecycle.createUser(newUser.toDomain()).toDto()
    } catch (e: UserEmailAlreadyExistsException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with this email already exists")
    }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN') and #principal.user.id != #id")
  fun delete(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    userRepository.findByIdOrNull(id)?.let {
      userLifecycle.deleteUser(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN') and #principal.user.id != #id")
  fun updateUser(
    @PathVariable id: String,
    @Valid @RequestBody
    patch: UserUpdateDto,
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ) {
    userRepository.findByIdOrNull(id)?.let { existing ->
      val updatedUser =
        with(patch) {
          existing.copy(
            roles = if (isSet("roles")) UserRoles.valuesOf(roles!!) else existing.roles,
            sharedAllLibraries = if (isSet("sharedLibraries")) sharedLibraries!!.all else existing.sharedAllLibraries,
            sharedLibrariesIds =
              if (isSet("sharedLibraries")) {
                if (sharedLibraries!!.all)
                  emptySet()
                else
                  libraryRepository.findAllByIds(sharedLibraries!!.libraryIds).map { it.id }.toSet()
              } else {
                existing.sharedLibrariesIds
              },
            restrictions =
              ContentRestrictions(
                ageRestriction =
                  if (isSet("ageRestriction")) {
                    if (ageRestriction == null)
                      null
                    else
                      AgeRestriction(ageRestriction!!.age, ageRestriction!!.restriction)
                  } else {
                    existing.restrictions.ageRestriction
                  },
                labelsAllow =
                  if (isSet("labelsAllow"))
                    labelsAllow
                      ?: emptySet()
                  else
                    existing.restrictions.labelsAllow,
                labelsExclude =
                  if (isSet("labelsExclude"))
                    labelsExclude
                      ?: emptySet()
                  else
                    existing.restrictions.labelsExclude,
              ),
          )
        }
      userLifecycle.updateUser(updatedUser)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("{id}/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN') or #principal.user.id == #id")
  fun updatePassword(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Valid @RequestBody
    newPasswordDto: PasswordUpdateDto,
  ) {
    if (demo) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    userRepository.findByIdOrNull(id)?.let { user ->
      userLifecycle.updatePassword(user, newPasswordDto.password, user.id != principal.user.id)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping("me/authentication-activity")
  @PageableAsQueryParam
  fun getMyAuthenticationActivity(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<AuthenticationActivityDto> {
    if (demo && !principal.user.isAdmin) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    val sort =
      if (page.sort.isSorted)
        page.sort
      else
        Sort.by(Sort.Order.desc("dateTime"))

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return authenticationActivityRepository.findAllByUser(principal.user, pageRequest).map { it.toDto() }
  }

  @GetMapping("authentication-activity")
  @PageableAsQueryParam
  @PreAuthorize("hasRole('ADMIN')")
  fun getAuthenticationActivity(
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): Page<AuthenticationActivityDto> {
    val sort =
      if (page.sort.isSorted)
        page.sort
      else
        Sort.by(Sort.Order.desc("dateTime"))

    val pageRequest =
      if (unpaged)
        UnpagedSorted(sort)
      else
        PageRequest.of(
          page.pageNumber,
          page.pageSize,
          sort,
        )

    return authenticationActivityRepository.findAll(pageRequest).map { it.toDto() }
  }

  @GetMapping("{id}/authentication-activity/latest")
  @PreAuthorize("hasRole('ADMIN') or #principal.user.id == #id")
  fun getLatestAuthenticationActivityForUser(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestParam(required = false, name = "apikey_id") apiKeyId: String?,
  ): AuthenticationActivityDto =
    userRepository.findByIdOrNull(id)?.let { user ->
      authenticationActivityRepository.findMostRecentByUser(user, apiKeyId)?.toDto()
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

  @GetMapping("me/api-keys")
  fun getApiKeys(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): Collection<ApiKeyDto> {
    if (demo && !principal.user.isAdmin) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    return userRepository.findApiKeyByUserId(principal.user.id).map { it.toDto().redacted() }
  }

  @PostMapping("me/api-keys")
  fun createApiKey(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @Valid @RequestBody apiKeyRequest: ApiKeyRequestDto,
  ): ApiKeyDto {
    if (demo && !principal.user.isAdmin) throw ResponseStatusException(HttpStatus.FORBIDDEN)
    return try {
      userLifecycle.createApiKey(principal.user, apiKeyRequest.comment)?.toDto()
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.code)
    }
      ?: throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to generate API key")
  }

  @DeleteMapping("me/api-keys/{keyId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteApiKey(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @PathVariable keyId: String,
  ) {
    if (!userRepository.existsApiKeyByIdAndUserId(keyId, principal.user.id))
      throw ResponseStatusException(HttpStatus.NOT_FOUND)
    userRepository.deleteApiKeyByIdAndUserId(keyId, principal.user.id)
  }
}
