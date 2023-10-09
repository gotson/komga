package org.gotson.komga.interfaces.api.rest

import jakarta.validation.Valid
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.interfaces.api.rest.dto.SettingsDto
import org.gotson.komga.interfaces.api.rest.dto.SettingsUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Duration.Companion.days

@RestController
@RequestMapping(value = ["api/v1/settings"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('$ROLE_ADMIN')")
class SettingsController(
  private val komgaSettingsProvider: KomgaSettingsProvider,
) {

  @GetMapping
  fun getSettings(): SettingsDto =
    SettingsDto(
      komgaSettingsProvider.deleteEmptyCollections,
      komgaSettingsProvider.deleteEmptyReadLists,
      komgaSettingsProvider.rememberMeDuration.inWholeDays,
    )

  @PatchMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateSettings(
    @Valid @RequestBody
    newSettings: SettingsUpdateDto,
  ) {
    newSettings.deleteEmptyCollections?.let { komgaSettingsProvider.deleteEmptyCollections = it }
    newSettings.deleteEmptyReadLists?.let { komgaSettingsProvider.deleteEmptyReadLists = it }
    newSettings.rememberMeDurationDays?.let { komgaSettingsProvider.rememberMeDuration = it.days }
    if (newSettings.renewRememberMeKey == true) komgaSettingsProvider.renewRememberMeKey()
  }
}
