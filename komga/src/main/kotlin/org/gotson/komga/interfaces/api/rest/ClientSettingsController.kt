package org.gotson.komga.interfaces.api.rest

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.gotson.komga.infrastructure.jooq.main.ClientSettingsDtoDao
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingDto
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingGlobalUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingUserUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

private const val KEY_REGEX = """[a-z]+(?:\.[a-z]+)*"""

@RestController
@RequestMapping(value = ["api/v1/client-settings"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
class ClientSettingsController(
  private val clientSettingsDtoDao: ClientSettingsDtoDao,
) {
  @GetMapping("global/list")
  fun getGlobalSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal?,
  ): Map<String, ClientSettingDto> = clientSettingsDtoDao.findAllGlobal(principal == null)

  @GetMapping("user/list")
  fun getUserSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal,
  ): Map<String, ClientSettingDto> = clientSettingsDtoDao.findAllUser(principal.user.id)

  @PatchMapping("global")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  fun saveGlobalSetting(
    @RequestBody newSettings: Map<
      @Pattern(regexp = KEY_REGEX)
      String,
      @NotNull @Valid
      ClientSettingGlobalUpdateDto,
      >,
  ) {
    newSettings.forEach { (key, setting) ->
      clientSettingsDtoDao.saveGlobal(key, setting.value, setting.allowUnauthorized)
    }
  }

  @PatchMapping("user")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun saveUserSetting(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody newSettings: Map<
      @Pattern(regexp = KEY_REGEX)
      String,
      @NotNull @Valid
      ClientSettingUserUpdateDto,
      >,
  ) {
    newSettings.forEach { (key, setting) ->
      clientSettingsDtoDao.saveForUser(principal.user.id, key, setting.value)
    }
  }

  @DeleteMapping("global")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteGlobalSetting(
    @RequestBody keysToDelete: Set<
      @Pattern(regexp = KEY_REGEX)
      String,
      >,
  ) {
    clientSettingsDtoDao.deleteGlobalByKeys(keysToDelete)
  }

  @DeleteMapping("user")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteGlobalSetting(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody keysToDelete: Set<
      @Pattern(regexp = KEY_REGEX)
      String,
      >,
  ) {
    clientSettingsDtoDao.deleteByUserIdAndKeys(principal.user.id, keysToDelete)
  }
}
