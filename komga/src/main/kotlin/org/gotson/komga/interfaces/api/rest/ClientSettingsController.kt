package org.gotson.komga.interfaces.api.rest

import org.gotson.komga.infrastructure.jooq.main.ClientSettingsDtoDao
import org.gotson.komga.infrastructure.security.KomgaPrincipal
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingDto
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingGlobalUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.ClientSettingUserUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["api/v1/client-settings"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ClientSettingsController(
  private val clientSettingsDtoDao: ClientSettingsDtoDao,
) {

  @GetMapping("list")
  fun getSettings(
    @AuthenticationPrincipal principal: KomgaPrincipal?,
  ): Collection<ClientSettingDto> {
    if (principal == null) {
      // return only global settings that allow unauthorized access
      return clientSettingsDtoDao
        .findAllGlobal()
        .filter { it.allowUnauthorized == true }
        .map { it.copy(allowUnauthorized = null) }
    }

    // merge global and user settings
    val global = clientSettingsDtoDao.findAllGlobal().associateBy { it.key }
    val user = clientSettingsDtoDao.findAllUser(principal.user.id).associateBy { it.key }

    val settings = (global + user).values
      .let {
        // if the user is not admin, we sanitize the allowUnauthorized values
        if (!principal.user.isAdmin) it.map { s -> s.copy(allowUnauthorized = null) }
        else it
      }

    return settings
  }

  @PutMapping("global")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  fun saveGlobalSetting(
    @RequestBody newSetting: ClientSettingGlobalUpdateDto,
  ) {
    clientSettingsDtoDao.saveGlobal(newSetting.key, newSetting.value, newSetting.allowUnauthorized)
  }

  @PutMapping("user")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun saveUserSetting(
    @AuthenticationPrincipal principal: KomgaPrincipal,
    @RequestBody newSetting: ClientSettingUserUpdateDto,
  ) {
    clientSettingsDtoDao.saveForUser(principal.user.id, newSetting.key, newSetting.value)
  }
}
