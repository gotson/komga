package org.gotson.komga.interfaces.api.rest

import jakarta.servlet.ServletContext
import jakarta.validation.Valid
import org.gotson.komga.domain.model.ROLE_ADMIN
import org.gotson.komga.infrastructure.configuration.KomgaSettingsProvider
import org.gotson.komga.interfaces.api.rest.dto.SettingMultiSource
import org.gotson.komga.interfaces.api.rest.dto.SettingsDto
import org.gotson.komga.interfaces.api.rest.dto.SettingsUpdateDto
import org.gotson.komga.interfaces.api.rest.dto.toDomain
import org.gotson.komga.interfaces.api.rest.dto.toDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.web.ServerProperties
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
  @Value("\${server.port:#{null}}") private val configServerPort: Int?,
  @Value("\${server.servlet.context-path:#{null}}") private val configServerContextPath: String?,
  serverProperties: ServerProperties,
  servletContext: ServletContext,
) {

  private val effectiveServerPort = serverProperties.port
  private val effectiveServerContextPath = servletContext.contextPath

  @GetMapping
  fun getSettings(): SettingsDto =
    SettingsDto(
      komgaSettingsProvider.deleteEmptyCollections,
      komgaSettingsProvider.deleteEmptyReadLists,
      komgaSettingsProvider.rememberMeDuration.inWholeDays,
      komgaSettingsProvider.thumbnailSize.toDto(),
      komgaSettingsProvider.taskPoolSize,
      SettingMultiSource(configServerPort, komgaSettingsProvider.serverPort, effectiveServerPort),
      SettingMultiSource(configServerContextPath, komgaSettingsProvider.serverContextPath, effectiveServerContextPath),
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
    newSettings.thumbnailSize?.let { komgaSettingsProvider.thumbnailSize = it.toDomain() }
    newSettings.taskPoolSize?.let { komgaSettingsProvider.taskPoolSize = it }

    if (newSettings.isSet("serverPort")) komgaSettingsProvider.serverPort = newSettings.serverPort
    if (newSettings.isSet("serverContextPath")) komgaSettingsProvider.serverContextPath = newSettings.serverContextPath
  }
}
